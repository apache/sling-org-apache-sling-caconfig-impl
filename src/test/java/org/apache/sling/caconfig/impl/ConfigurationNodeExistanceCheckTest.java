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

import static org.apache.sling.caconfig.resource.impl.def.ConfigurationResourceNameConstants.PROPERTY_CONFIG_REF;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import edu.umd.cs.findbugs.annotations.When;

/**
 * Test {@link ConfigurationResolver} with custom adaptions (in this case: Sling
 * Models) for reading the config.
 */
public class ConfigurationNodeExistanceCheckTest {

    @Rule
    public SlingContext context = new SlingContext();
   
    @Mock
    private ConfigurationBuilder configBuilder;
    
    private Resource contentResource;
    
    @Mock
    private SlingHttpServletRequest request;

    @Before
    public void setUp() {
        contentResource = context.create().resource("/content/site1/page1");
        configBuilder = contentResource.adaptTo(ConfigurationBuilder.class);
        // content resources
        context.build().resource("/content/site1", PROPERTY_CONFIG_REF, "/conf/content/site1");
    }

    @Test
    public void testNonExistingConfigResource() {
        boolean checkIfNodeExists = configBuilder.has("/conf/content/site2");
        assertEquals(false, checkIfNodeExists);
    }

    @Test
    public void testIncorrectConfigName() {
        boolean checkIfNodeExists = configBuilder.has("/conf/content/site3");
        assertEquals(false, checkIfNodeExists);
    }

    @Test
    public void testConfigExists() {
        boolean checkIfNodeExists = configBuilder.has("/conf/content/site1");
        assertEquals(true, checkIfNodeExists);
    }
}
