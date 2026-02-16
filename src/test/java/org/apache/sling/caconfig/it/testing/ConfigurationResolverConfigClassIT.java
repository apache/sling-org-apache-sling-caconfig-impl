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
package org.apache.sling.caconfig.it.testing;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.it.testbundle.example.SimpleConfig;
import org.apache.sling.junit.rules.TeleporterRule;
import org.apache.sling.resourcebuilder.api.ResourceBuilder;
import org.apache.sling.resourcebuilder.api.ResourceBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.apache.sling.caconfig.it.testing.TestUtils.CONFIG_ROOT_PATH;
import static org.apache.sling.caconfig.it.testing.TestUtils.CONTENT_ROOT_PATH;
import static org.apache.sling.caconfig.it.testing.TestUtils.cleanUp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ConfigurationResolverConfigClassIT {

    @Rule
    public TeleporterRule teleporter = TeleporterRule.forClass(getClass(), "IT");

    private ResourceResolver resourceResolver;
    private ResourceBuilder resourceBuilder;

    private static final String PAGE_PATH = CONTENT_ROOT_PATH + "/page1";
    private static final String CONFIG_PATH = CONFIG_ROOT_PATH + "/page1";

    @Before
    public void setUp() throws Exception {
        resourceResolver = teleporter.getService(ResourceResolverFactory.class).getServiceResourceResolver(null);
        resourceBuilder = teleporter.getService(ResourceBuilderFactory.class).forResolver(resourceResolver);
    }

    @After
    public void tearDown() {
        cleanUp(resourceResolver);
        resourceResolver.close();
    }

    @Test
    public void testNonExistingConfig() {
        Resource resourcePage1 = resourceBuilder.resource(PAGE_PATH).getCurrentParent();

        ConfigurationResolver configResolver = teleporter.getService(ConfigurationResolver.class);
        SimpleConfig config = configResolver.get(resourcePage1).name("test").as(SimpleConfig.class);
        assertNotNull(config);

        assertNull(config.stringParam());
        assertEquals("defValue", config.stringParamDefault());
        assertEquals(0, config.intParam());
        assertEquals(false, config.boolParam());
    }

    @Test
    @SuppressWarnings("null")
    public void testExistingConfig() {
        resourceBuilder
                .resource(
                        CONFIG_PATH + "/sling:configs/test",
                        "stringParam",
                        "value1",
                        "intParam",
                        123,
                        "boolParam",
                        true)
                .resource(PAGE_PATH, "sling:configRef", CONFIG_PATH);

        Resource resourcePage1 = resourceResolver.getResource(PAGE_PATH);

        ConfigurationResolver configResolver = teleporter.getService(ConfigurationResolver.class);
        SimpleConfig config = configResolver.get(resourcePage1).name("test").as(SimpleConfig.class);
        assertNotNull(config);

        assertEquals("value1", config.stringParam());
        assertEquals("defValue", config.stringParamDefault());
        assertEquals(123, config.intParam());
        assertEquals(true, config.boolParam());
    }
}
