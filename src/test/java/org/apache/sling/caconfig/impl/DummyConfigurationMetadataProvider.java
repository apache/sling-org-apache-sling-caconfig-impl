/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.caconfig.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.caconfig.spi.ConfigurationMetadataProvider;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;

class DummyConfigurationMetadataProvider implements ConfigurationMetadataProvider {

    private final String configName;
    private final Map<String, Object> defaultValues;
    private final boolean collection;

    public DummyConfigurationMetadataProvider(
            String configName, Map<String, Object> defaultValues, boolean collection) {
        this.configName = configName;
        this.defaultValues = defaultValues;
        this.collection = collection;
    }

    @Override
    public @NotNull SortedSet<String> getConfigurationNames() {
        return ImmutableSortedSet.of(configName);
    }

    @Override
    public ConfigurationMetadata getConfigurationMetadata(String configName) {
        if (!StringUtils.equals(this.configName, configName)) {
            return null;
        }
        List<PropertyMetadata<?>> properties = new ArrayList<>();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            properties.add(new PropertyMetadata<>(entry.getKey(), entry.getValue()));
        }
        return new ConfigurationMetadata(configName, properties, collection);
    }
}
