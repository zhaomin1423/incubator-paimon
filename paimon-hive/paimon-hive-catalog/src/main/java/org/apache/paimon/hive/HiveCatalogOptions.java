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

package org.apache.paimon.hive;

import org.apache.paimon.options.ConfigOption;
import org.apache.paimon.options.ConfigOptions;

/** Options for hive catalog. */
public final class HiveCatalogOptions {

    public static final String IDENTIFIER = "hive";

    public static final ConfigOption<String> HIVE_CONF_DIR =
            ConfigOptions.key("hive-conf-dir")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "File directory of the hive-site.xml , used to create HiveMetastoreClient and security authentication, such as Kerberos, LDAP, Ranger and so on");

    public static final ConfigOption<String> HADOOP_CONF_DIR =
            ConfigOptions.key("hadoop-conf-dir")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "File directory of the core-site.xml、hdfs-site.xml、yarn-site.xml、mapred-site.xml. Currently, only local file system paths are supported.");

    public static final ConfigOption<Boolean> LOCATION_IN_PROPERTIES =
            ConfigOptions.key("location-in-properties")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "Setting the location in properties of hive table/database.\n"
                                    + "If you don't want to access the location by the filesystem of hive when using a object storage such as s3,oss\n"
                                    + "you can set this option to true.\n");

    private HiveCatalogOptions() {}
}
