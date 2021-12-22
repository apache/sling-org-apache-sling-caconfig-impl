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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationInjectResourceDetectionStrategyMultiplexerImplTest {

    @Rule
    public SlingContext context = new SlingContext();

    @Mock
    private SlingHttpServletRequest request;
    @Mock
    private Resource resource1;
    @Mock
    private Resource resource2;

    private ConfigurationInjectResourceDetectionStrategy underTest;

    @Before
    public void setUp() {
        underTest = context.registerInjectActivateService(new ConfigurationInjectResourceDetectionStrategyMultiplexerImpl());
    }

    @Test
    public void testWithNoStrategies() {
        assertNull(underTest.detectResource(request));
    }

    @Test
    @SuppressWarnings("null")
    public void testWithOneStrategy() {
        ConfigurationInjectResourceDetectionStrategy strategy = mock(ConfigurationInjectResourceDetectionStrategy.class);
        when(strategy.detectResource(request)).thenReturn(resource1);
        context.registerService(ConfigurationInjectResourceDetectionStrategy.class, strategy);

        assertSame(resource1, underTest.detectResource(request));
    }

    @Test
    @SuppressWarnings("null")
    public void testWithMultipleStrategies() {
        ConfigurationInjectResourceDetectionStrategy strategy1 = mock(ConfigurationInjectResourceDetectionStrategy.class);
        ConfigurationInjectResourceDetectionStrategy strategy2 = mock(ConfigurationInjectResourceDetectionStrategy.class);
        ConfigurationInjectResourceDetectionStrategy strategy3 = mock(ConfigurationInjectResourceDetectionStrategy.class);

        when(strategy1.detectResource(request)).thenReturn(null);
        when(strategy2.detectResource(request)).thenReturn(resource2);

        context.registerService(ConfigurationInjectResourceDetectionStrategy.class, strategy1);
        context.registerService(ConfigurationInjectResourceDetectionStrategy.class, strategy2);
        context.registerService(ConfigurationInjectResourceDetectionStrategy.class, strategy3);

        assertSame(resource2, underTest.detectResource(request));

        verify(strategy1, times(1)).detectResource(request);
        verify(strategy2, times(1)).detectResource(request);
        verifyNoMoreInteractions(strategy3);
    }

}
