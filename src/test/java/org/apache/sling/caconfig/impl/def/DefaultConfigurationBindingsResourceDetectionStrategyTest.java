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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.caconfig.resource.spi.ConfigurationBindingsResourceDetectionStrategy;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigurationBindingsResourceDetectionStrategyTest {

    @Rule
    public SlingContext context = new SlingContext();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private Resource resource;

    @Mock
    private Bindings bindings;

    private ConfigurationBindingsResourceDetectionStrategy underTest;

    @Before
    public void before() {
        underTest = context.registerService(ConfigurationBindingsResourceDetectionStrategy.class, new DefaultConfigurationBindingsResourceDetectionStrategy());
        when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(true);
        when(bindings.get(SlingBindings.REQUEST)).thenReturn(request);
        when(request.getResource()).thenReturn(resource);
    }

    @Test
    public void testNoResource() {
        when(request.getResource()).thenReturn(null);
        assertNull(underTest.detectResource(bindings));
    }

    @Test
    public void testNoRequest() {
        when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(false);
        assertNull(underTest.detectResource(bindings));
    }

    @Test
    public void testWithResource() {
        when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(true);
        assertSame(resource, underTest.detectResource(bindings));
    }

}
