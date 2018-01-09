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
package org.apache.sling.caconfig.impl.def;

import static org.apache.sling.caconfig.impl.def.ConfigurationDefNameConstants.PROPERTY_CONFIG_PROPERTY_INHERIT;
import static org.apache.sling.caconfig.resource.impl.def.ConfigurationResourceNameConstants.PROPERTY_CONFIG_REF;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.impl.ConfigurationTestUtils;
import org.apache.sling.caconfig.resource.impl.def.DefaultConfigurationResourceResolvingStrategy;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Test {@link ConfigurationResolver} with property inheritance and merging with runmode-aware configuration resolving strategy.
 */
public class ConfigurationResolverPropertyInheritanceRunModeTest {

    @Rule
    public SlingContext context = new SlingContext();

    private ConfigurationResolver underTest;

    private Resource site1Page1;
    private Resource site2Page1;

    @Before
    public void setUp() throws IOException {
        
        // enable runModeAware for DefaultConfigurationResourceResolvingStrategy
        ConfigurationAdmin configAdmin = context.getService(ConfigurationAdmin.class);
        Configuration serviceConfig = configAdmin.getConfiguration(DefaultConfigurationResourceResolvingStrategy.class.getName());
        Dictionary<String,Object> configProps = new Hashtable<>();
        configProps.put("runModeAware", true);
        serviceConfig.update(configProps);
        
        underTest = ConfigurationTestUtils.registerConfigurationResolver(context);

        // content resources that form a deeper hierarchy
        context.build()
            .resource("/content/tenant1", PROPERTY_CONFIG_REF, "/conf/brand1/tenant1")
            .resource("/content/tenant1/region1", PROPERTY_CONFIG_REF, "/conf/brand1/tenant1/region1")
            .resource("/content/tenant1/region1/site1", PROPERTY_CONFIG_REF, "/conf/brand1/tenant1/region1/site1")
            .resource("/content/tenant1/region1/site2", PROPERTY_CONFIG_REF, "/conf/brand1/tenant1/region1/site2");
        site1Page1 = context.create().resource("/content/tenant1/region1/site1/page1");
        site2Page1 = context.create().resource("/content/tenant1/region1/site2/page1");

        // set sample run mode
        context.runMode("runmode1");
    }

    @Test
    public void testInheritanceWithoutMerging() {
        context.build()
            .resource("/conf/global/sling:configs/test", "param1", "value1", "param2", "value2", "param3", "value3")
            .resource("/conf/global/sling:configs.runmode1/test", "param1", "value1.1", "param2", "value2.1")
            .resource("/conf/global/sling:configs.runmode2/test", "param1", "value1.2", "param2", "value2.2")
            .resource("/conf/brand1/tenant1/region1/site1/sling:configs/test", "param1", "value1a", "param3", "value3a")
            .resource("/conf/brand1/tenant1/region1/site1/sling:configs.runmode1/test", "param1", "value1a.1")
            .resource("/conf/brand1/tenant1/region1/site1/sling:configs.runmode2/test", "param1", "value1a.2")
            .resource("/conf/brand1/tenant1/region1/site2/sling:configs/test", "param1", "value1b");
        
        assertThat(underTest.get(site1Page1).name("test").asValueMap(), allOf(
                        hasEntry("param1", (Object)"value1a.1"),
                        not(hasKey("param2")),
                        not(hasKey("param3"))));
        assertThat(underTest.get(site2Page1).name("test").asValueMap(), allOf(
                        hasEntry("param1", (Object)"value1b"),
                        not(hasKey("param2")),
                        not(hasKey("param3"))));
    }

    @Test
    public void testInheritanceMerging() {
        context.build()
            .resource("/conf/global/sling:configs/test", "param1", "value1", "param2", "value2", "param3", "value3")
            .resource("/conf/global/sling:configs.runmode1/test", "param1", "value1.1", "param2", "value2.1")
            .resource("/conf/global/sling:configs.runmode2/test", "param1", "value1.2", "param2", "value2.2")
            .resource("/conf/brand1/tenant1/region1/site1/sling:configs/test", "param1", "value1a", "param3", "value3a",
                    PROPERTY_CONFIG_PROPERTY_INHERIT, true)
            .resource("/conf/brand1/tenant1/region1/site1/sling:configs.runmode1/test", "param1", "value1a.1",
                    PROPERTY_CONFIG_PROPERTY_INHERIT, true)
            .resource("/conf/brand1/tenant1/region1/site1/sling:configs.runmode2/test", "param1", "value1a.2",
                    PROPERTY_CONFIG_PROPERTY_INHERIT, true)
            .resource("/conf/brand1/tenant1/region1/site2/sling:configs/test", "param1", "value1b");
        
        assertThat(underTest.get(site1Page1).name("test").asValueMap(), allOf(
                hasEntry("param1", (Object)"value1a.1"),
                hasEntry("param2", (Object)"value2.1"),
                not(hasKey("param3"))));        
        assertThat(underTest.get(site2Page1).name("test").asValueMap(), allOf(
                hasEntry("param1", (Object)"value1b"),
                not(hasKey("param2")),
                not(hasKey("param3"))));
    }

}
