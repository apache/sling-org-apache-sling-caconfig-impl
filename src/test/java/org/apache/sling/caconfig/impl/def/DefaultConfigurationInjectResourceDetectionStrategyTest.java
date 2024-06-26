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

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.spi.ConfigurationInjectResourceDetectionStrategy;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigurationInjectResourceDetectionStrategyTest {

    @Rule
    public SlingContext context = new SlingContext();

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private Resource resource;

    @Mock
    private Bindings bindings;

    private ConfigurationInjectResourceDetectionStrategy underTest;

    @Before
    public void before() {
        underTest = context.registerService(
                ConfigurationInjectResourceDetectionStrategy.class,
                new DefaultConfigurationInjectResourceDetectionStrategy());
        when(request.getResource()).thenReturn(resource);
    }

    @Test
    @SuppressWarnings("null")
    public void testNoResource() {
        when(request.getResource()).thenReturn(null);
        assertNull(underTest.detectResource(request));
    }

    @Test
    public void testWithResource() {
        assertSame(resource, underTest.detectResource(request));
    }
}
