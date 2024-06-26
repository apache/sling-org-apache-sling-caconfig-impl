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

import javax.script.Bindings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationInjectResourceDetectionStrategyMultiplexer;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationMetadataProviderMultiplexer;
import org.apache.sling.caconfig.spi.ConfigurationMetadataProvider;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Binds a script variable "caconfig" to the current configuration value map to be
 * used in HTL (Sightly).
 */
@Component(immediate = true, service = BindingsValuesProvider.class)
@ServiceRanking(100)
@Designate(ocd = ConfigurationBindingsValueProvider.Config.class)
public class ConfigurationBindingsValueProvider implements BindingsValuesProvider {

    /**
     * Name of the variable to which the config value map is bound to in script
     * configuration.
     */
    public static final String BINDING_VARIABLE = "caconfig";

    @ObjectClassDefinition(
            name = "Apache Sling Context-Aware Configuration Bindings Value Provider",
            description = "Binds a script variable '" + BINDING_VARIABLE + "' to the scripting context.")
    static @interface Config {

        @AttributeDefinition(name = "Enabled", description = "Enable provider.")
        boolean enabled() default true;

        @AttributeDefinition(
                name = "Scripting Engines",
                description = "Enable bindings value provider for the given scripting engines.")
        String[] javax_script_name() default {"sightly"};
    }

    @Reference
    private ConfigurationMetadataProviderMultiplexer configMetadataProvider;

    @Reference
    private ConfigurationInjectResourceDetectionStrategyMultiplexer configurationInjectResourceDetectionStrategy;

    private boolean enabled;

    @Override
    public void addBindings(Bindings bindings) {
        if (!enabled) {
            return;
        }

        Resource resource = detectResourceForInjection(bindings);
        if (resource == null) {
            return;
        }

        Map<String, Object> configMap = new ConfigMap(resource, configMetadataProvider);
        bindings.put(BINDING_VARIABLE, configMap);
    }

    private Resource detectResourceForInjection(Bindings bindings) {
        SlingHttpServletRequest request = (SlingHttpServletRequest) bindings.get(SlingBindings.REQUEST);
        if (request != null) {
            return configurationInjectResourceDetectionStrategy.detectResource(request);
        }
        return (Resource) bindings.get(SlingBindings.RESOURCE);
    }

    @Activate
    void activate(Config config) {
        this.enabled = config.enabled();
    }

    /**
     * This is a "virtual" containing configuration names as keys, and the underlying value maps/value map collections as values.
     * The map accesses only the data that is really required in a lazy fashion.
     */
    private static class ConfigMap implements Map<String, Object> {

        private final Resource resource;
        private final ConfigurationMetadataProvider configMetadataProvider;
        private Set<String> configNamesCache;
        private Map<String, Object> valuesCache = new HashMap<>();

        ConfigMap(Resource resource, ConfigurationMetadataProvider configMetadataProvider) {
            this.resource = resource;
            this.configMetadataProvider = configMetadataProvider;
        }

        private Set<String> getConfigNames() {
            if (configNamesCache == null) {
                configNamesCache = configMetadataProvider.getConfigurationNames();
            }
            return configNamesCache;
        }

        @Override
        public int size() {
            return getConfigNames().size();
        }

        @Override
        public boolean isEmpty() {
            return getConfigNames().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return getConfigNames().contains(key);
        }

        @Override
        public Object get(Object key) {
            Object value = valuesCache.get(key);
            if (value == null) {
                value = getConfigValue((String) key);
                if (value != null) {
                    valuesCache.put((String) key, value);
                }
            }
            return value;
        }

        private Object getConfigValue(String configName) {
            @SuppressWarnings("null")
            ConfigurationBuilder configBuilder =
                    resource.adaptTo(ConfigurationBuilder.class).name(configName);
            if (isCollection(configName)) {
                return configBuilder.asValueMapCollection();
            } else {
                return configBuilder.asValueMap();
            }
        }

        private boolean isCollection(String configName) {
            ConfigurationMetadata configMetadata = configMetadataProvider.getConfigurationMetadata(configName);
            if (configMetadata != null) {
                return configMetadata.isCollection();
            } else {
                return false;
            }
        }

        @Override
        public Set<String> keySet() {
            return getConfigNames();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Object> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException();
        }
    }
}
