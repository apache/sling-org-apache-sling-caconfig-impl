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
package org.apache.sling.caconfig.management.impl;

import com.google.common.collect.ImmutableList;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.caconfig.impl.def.DefaultConfigurationPersistenceStrategy;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy2;
import org.apache.sling.testing.mock.osgi.MapUtil;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ConfigurationPersistenceStrategyMultiplexerImplTest {

    @Rule
    public SlingContext context = new SlingContext();

    private ConfigurationPersistenceStrategyMultiplexer underTest;
    private ConfigurationPersistenceStrategyBridge bridge;

    private Resource resource1;
    private Resource resource2;

    @Before
    public void setUp() {
        context.registerInjectActivateService(ConfigurationManagementSettingsImpl.class);
        underTest = context.registerInjectActivateService(ConfigurationPersistenceStrategyMultiplexerImpl.class);

        bridge = MockOsgi.activateInjectServices(ConfigurationPersistenceStrategyBridge.class, context.bundleContext());

        resource1 = context.create().resource("/conf/test1");
        resource2 = context.create().resource("/conf/test2");
    }

    @Test
    public void testWithNoStrategies() {
        assertNull(underTest.getResource(resource1));
        assertNull(underTest.getResourcePath(resource1.getPath()));
        assertFalse(underTest.persistConfiguration(
                context.resourceResolver(), "/conf/test1", new ConfigurationPersistData(resource1.getValueMap())));
        assertFalse(underTest.persistConfigurationCollection(
                context.resourceResolver(),
                "/conf/testCol",
                new ConfigurationCollectionPersistData(ImmutableList.of(
                        new ConfigurationPersistData(resource1.getValueMap()).collectionItemName(resource1.getName()),
                        new ConfigurationPersistData(resource2.getValueMap())
                                .collectionItemName(resource2.getName())))));
        assertFalse(underTest.deleteConfiguration(context.resourceResolver(), "/conf/test1"));
    }

    @Test
    public void testWithDefaultStrategy() {
        context.registerInjectActivateService(new DefaultConfigurationPersistenceStrategy());

        assertSame(resource1, underTest.getResource(resource1));
        assertEquals(resource1.getPath(), underTest.getResourcePath(resource1.getPath()));
        assertTrue(underTest.persistConfiguration(
                context.resourceResolver(), "/conf/test1", new ConfigurationPersistData(resource1.getValueMap())));
        assertTrue(underTest.persistConfigurationCollection(
                context.resourceResolver(),
                "/conf/testCol",
                new ConfigurationCollectionPersistData(ImmutableList.of(
                        new ConfigurationPersistData(resource1.getValueMap()).collectionItemName(resource1.getName()),
                        new ConfigurationPersistData(resource2.getValueMap())
                                .collectionItemName(resource2.getName())))));
        assertTrue(underTest.deleteConfiguration(context.resourceResolver(), "/conf/test1"));
    }

    @Test
    @SuppressWarnings({"deprecation", "null"})
    public void testMultipleStrategies() {

        // strategy 1 (using old ConfigurationPersistenceStrategy with bridge to ConfigurationPersistenceStrategy2)
        org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy oldStrategy = context.registerService(
                org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy.class,
                new org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy() {
                    @Override
                    public Resource getResource(@NotNull Resource resource) {
                        return resource2;
                    }

                    @Override
                    public String getResourcePath(@NotNull String resourcePath) {
                        return resource2.getPath();
                    }

                    @Override
                    public boolean persistConfiguration(
                            @NotNull ResourceResolver resourceResolver,
                            @NotNull String configResourcePath,
                            @NotNull ConfigurationPersistData data) {
                        return true;
                    }

                    @Override
                    public boolean persistConfigurationCollection(
                            @NotNull ResourceResolver resourceResolver,
                            @NotNull String configResourceCollectionParentPath,
                            @NotNull ConfigurationCollectionPersistData data) {
                        return false;
                    }

                    @Override
                    public boolean deleteConfiguration(
                            @NotNull ResourceResolver resourceResolver, @NotNull String configResourcePath) {
                        return false;
                    }
                },
                Constants.SERVICE_RANKING,
                2000);

        // bind manually as mock-osgi currently does not support tracking references to OSGi components which are not
        // OSGi services
        bridge.bindConfigurationPersistenceStrategy(oldStrategy, MapUtil.toMap(Constants.SERVICE_RANKING, 2000));

        // strategy 2
        context.registerService(
                ConfigurationPersistenceStrategy2.class,
                new ConfigurationPersistenceStrategy2() {
                    @Override
                    public Resource getResource(@NotNull Resource resource) {
                        return resource1;
                    }

                    @Override
                    public Resource getCollectionParentResource(@NotNull Resource resource) {
                        return resource1;
                    }

                    @Override
                    public Resource getCollectionItemResource(@NotNull Resource resource) {
                        return resource1;
                    }

                    @Override
                    public String getResourcePath(@NotNull String resourcePath) {
                        return resource1.getPath();
                    }

                    @Override
                    public String getCollectionParentResourcePath(@NotNull String resourcePath) {
                        return resource1.getPath();
                    }

                    @Override
                    public String getCollectionItemResourcePath(@NotNull String resourcePath) {
                        return resource1.getPath();
                    }

                    @Override
                    public String getConfigName(@NotNull String configName, @Nullable String relatedConfigPath) {
                        return resource1.getPath();
                    }

                    @Override
                    public String getCollectionParentConfigName(
                            @NotNull String configName, @Nullable String relatedConfigPath) {
                        return resource1.getPath();
                    }

                    @Override
                    public String getCollectionItemConfigName(
                            @NotNull String configName, @Nullable String relatedConfigPath) {
                        return resource1.getPath();
                    }

                    @Override
                    public boolean persistConfiguration(
                            @NotNull ResourceResolver resourceResolver,
                            @NotNull String configResourcePath,
                            @NotNull ConfigurationPersistData data) {
                        return false;
                    }

                    @Override
                    public boolean persistConfigurationCollection(
                            @NotNull ResourceResolver resourceResolver,
                            @NotNull String configResourceCollectionParentPath,
                            @NotNull ConfigurationCollectionPersistData data) {
                        return true;
                    }

                    @Override
                    public boolean deleteConfiguration(
                            @NotNull ResourceResolver resourceResolver, @NotNull String configResourcePath) {
                        return true;
                    }
                },
                Constants.SERVICE_RANKING,
                1000);

        assertEquals(resource2.getPath(), underTest.getResource(resource1).getPath());
        assertEquals(
                resource1.getPath(),
                underTest.getCollectionParentResource(resource1).getPath());
        assertEquals(
                resource2.getPath(),
                underTest.getCollectionItemResource(resource1).getPath());
        assertEquals(resource2.getPath(), underTest.getResourcePath(resource1.getPath()));
        assertEquals(resource1.getPath(), underTest.getCollectionParentResourcePath(resource1.getPath()));
        assertEquals(resource2.getPath(), underTest.getCollectionItemResourcePath(resource1.getPath()));
        assertEquals(resource2.getPath(), underTest.getConfigName(resource1.getPath(), null));
        assertEquals(resource1.getPath(), underTest.getCollectionParentConfigName(resource1.getPath(), null));
        assertEquals(resource2.getPath(), underTest.getCollectionItemConfigName(resource1.getPath(), null));
        assertEquals(
                ImmutableList.of(resource2.getPath(), resource1.getPath()),
                ImmutableList.copyOf(underTest.getAllConfigNames(resource1.getPath())));
        assertEquals(
                ImmutableList.of(resource1.getPath()),
                ImmutableList.copyOf(underTest.getAllCollectionParentConfigNames(resource1.getPath())));
        assertEquals(
                ImmutableList.of(resource2.getPath(), resource1.getPath()),
                ImmutableList.copyOf(underTest.getAllCollectionItemConfigNames(resource1.getPath())));
        assertTrue(underTest.persistConfiguration(
                context.resourceResolver(), "/conf/test1", new ConfigurationPersistData(resource1.getValueMap())));
        assertTrue(underTest.persistConfigurationCollection(
                context.resourceResolver(),
                "/conf/testCol",
                new ConfigurationCollectionPersistData(ImmutableList.of(
                        new ConfigurationPersistData(resource1.getValueMap()).collectionItemName(resource1.getName()),
                        new ConfigurationPersistData(resource2.getValueMap())
                                .collectionItemName(resource2.getName())))));
        assertTrue(underTest.deleteConfiguration(context.resourceResolver(), "/conf/test1"));
    }
}
