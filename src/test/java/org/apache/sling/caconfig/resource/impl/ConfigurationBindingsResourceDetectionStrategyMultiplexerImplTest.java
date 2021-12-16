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
package org.apache.sling.caconfig.resource.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.script.Bindings;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.spi.ConfigurationBindingsResourceDetectionStrategy;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationBindingsResourceDetectionStrategyMultiplexerImplTest {

    @Rule
    public SlingContext context = new SlingContext();

    @Mock
    private Bindings bindings;
    @Mock
    private Resource resource1;
    @Mock
    private Resource resource2;

    private ConfigurationBindingsResourceDetectionStrategy underTest;

    @Before
    public void setUp() {
        underTest = context.registerInjectActivateService(new ConfigurationBindingsResourceDetectionStrategyMultiplexerImpl());
    }

    @Test
    public void testWithNoStrategies() {
        assertNull(underTest.detectResource(bindings));
    }

    @Test
    public void testWithOneStrategy() {
        ConfigurationBindingsResourceDetectionStrategy strategy = mock(ConfigurationBindingsResourceDetectionStrategy.class);
        when(strategy.detectResource(bindings)).thenReturn(resource1);
        context.registerService(ConfigurationBindingsResourceDetectionStrategy.class, strategy);

        assertSame(resource1, underTest.detectResource(bindings));
    }

    @Test
    public void testWithMultipleStrategies() {
        ConfigurationBindingsResourceDetectionStrategy strategy1 = mock(ConfigurationBindingsResourceDetectionStrategy.class);
        ConfigurationBindingsResourceDetectionStrategy strategy2 = mock(ConfigurationBindingsResourceDetectionStrategy.class);
        ConfigurationBindingsResourceDetectionStrategy strategy3 = mock(ConfigurationBindingsResourceDetectionStrategy.class);

        when(strategy1.detectResource(bindings)).thenReturn(null);
        when(strategy2.detectResource(bindings)).thenReturn(resource2);

        context.registerService(ConfigurationBindingsResourceDetectionStrategy.class, strategy1);
        context.registerService(ConfigurationBindingsResourceDetectionStrategy.class, strategy2);
        context.registerService(ConfigurationBindingsResourceDetectionStrategy.class, strategy3);

        assertSame(resource2, underTest.detectResource(bindings));

        verify(strategy1, times(1)).detectResource(bindings);
        verify(strategy2, times(1)).detectResource(bindings);
        verifyNoMoreInteractions(strategy3);
    }

}
