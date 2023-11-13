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
package org.apache.sling.caconfig.impl.models.via;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.impl.models.CAConfigViaProviderCacheProvider;
import org.apache.sling.caconfig.models.via.ContextAwareConfigResource;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertSame;
import static org.apache.sling.caconfig.resource.impl.def.ConfigurationResourceNameConstants.PROPERTY_CONFIG_REF;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CAConfigViaProviderTest {
    @Rule
    public SlingContext context = new SlingContext();

    @Mock
    private ConfigurationResolver configurationResolver;

    @Mock
    private CAConfigViaProviderCacheProvider cacheProvider;

    @InjectMocks
    private CAConfigViaProvider underTest;

    private Resource site1Page1;

    @Mock
    private ConfigurationBuilder configurationBuilder;
    @Mock
    private Resource configResource;

    @Before
    public void setUp(){

        context.addModelsForPackage("org.apache.sling.caconfig.example");

        // content resources
        context.build().resource("/content/site1", PROPERTY_CONFIG_REF, "/conf/content/site1");
        site1Page1 = context.create().resource("/content/site1/page1");

        when(configurationResolver.get(site1Page1)).thenReturn(configurationBuilder);
        when(configurationBuilder.name(anyString())).thenReturn(configurationBuilder);
        when(configurationBuilder.asAdaptable(Resource.class)).thenReturn(configResource);


    }

    @Test
    public void test_getAdaptable(){
        Resource adaptable = (Resource) underTest.getAdaptable(site1Page1, "testconfig");

        verify(configurationBuilder, times(1)).name("testconfig");
        verify(configurationBuilder, times(1)).asAdaptable(Resource.class);

        assertNotNull(adaptable);
        assertSame(configResource, adaptable);
    }

    @Test
    public void test_getType(){
        assertSame(ContextAwareConfigResource.class, underTest.getType());
    }
}
