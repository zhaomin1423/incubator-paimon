/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.consumer;

import org.apache.paimon.fs.FileIO;
import org.apache.paimon.fs.Path;
import org.apache.paimon.fs.PositionOutputStream;
import org.apache.paimon.utils.DateTimeUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import static org.apache.paimon.utils.FileUtils.listOriginalVersionedFiles;
import static org.apache.paimon.utils.FileUtils.listVersionedFileStatus;

/** Manage consumer groups. */
public class ConsumerManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CONSUMER_PREFIX = "consumer-";

    private final FileIO fileIO;
    private final Path tablePath;

    public ConsumerManager(FileIO fileIO, Path tablePath) {
        this.fileIO = fileIO;
        this.tablePath = tablePath;
    }

    public Optional<Consumer> consumer(String consumerId) {
        return Consumer.fromPath(fileIO, consumerPath(consumerId));
    }

    public void recordConsumer(String consumerId, Consumer consumer) {
        try (PositionOutputStream out = fileIO.newOutputStream(consumerPath(consumerId), true)) {
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write(consumer.toJson());
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OptionalLong minNextSnapshot() {
        try {
            return listOriginalVersionedFiles(fileIO, consumerDirectory(), CONSUMER_PREFIX)
                    .map(this::consumer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .mapToLong(Consumer::nextSnapshot)
                    .reduce(Math::min);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void expire(LocalDateTime expireDateTime) {
        try {
            listVersionedFileStatus(fileIO, consumerDirectory(), CONSUMER_PREFIX)
                    .forEach(
                            status -> {
                                LocalDateTime modificationTime =
                                        DateTimeUtils.toLocalDateTime(status.getModificationTime());
                                if (expireDateTime.isAfter(modificationTime)) {
                                    fileIO.deleteQuietly(status.getPath());
                                }
                            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Get all consumer. */
    public Map<String, Long> consumers() throws IOException {
        Map<String, Long> consumers = new HashMap<>();
        listOriginalVersionedFiles(fileIO, consumerDirectory(), CONSUMER_PREFIX)
                .forEach(
                        id -> {
                            Optional<Consumer> consumer = this.consumer(id);
                            consumer.ifPresent(value -> consumers.put(id, value.nextSnapshot()));
                        });
        return consumers;
    }

    /** List all consumer IDs. */
    public List<String> listAllIds() {
        try {
            return listOriginalVersionedFiles(fileIO, consumerDirectory(), CONSUMER_PREFIX)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path consumerDirectory() {
        return new Path(tablePath + "/consumer");
    }

    private Path consumerPath(String consumerId) {
        return new Path(tablePath + "/consumer/" + CONSUMER_PREFIX + consumerId);
    }
}
