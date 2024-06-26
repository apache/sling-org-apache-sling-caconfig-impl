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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.sling.caconfig.impl.ConfigurationProxy.CachingInvocationHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationProxyCachingInvocationHandlerTest {

    private InvocationHandler underTest;

    @Mock
    private InvocationHandler invocationHandler;

    private Object testObject;
    private Method testMethod;

    @Before
    public void setUp() throws Exception {
        underTest = new CachingInvocationHandler(invocationHandler);
        testObject = new Object();
        testMethod = Object.class.getMethod("toString");
    }

    @Test
    public void testCacheMiss() throws Throwable {
        when(invocationHandler.invoke(testObject, testMethod, null)).thenReturn("value1");
        assertEquals("value1", underTest.invoke(testObject, testMethod, null));
        verify(invocationHandler, times(1)).invoke(testObject, testMethod, null);
    }

    @Test
    public void testCacheMissNull() throws Throwable {
        when(invocationHandler.invoke(testObject, testMethod, null)).thenReturn(null);
        assertNull(underTest.invoke(testObject, testMethod, null));
        verify(invocationHandler, times(1)).invoke(testObject, testMethod, null);
    }

    @Test
    public void testCacheHit() throws Throwable {
        when(invocationHandler.invoke(testObject, testMethod, null)).thenReturn("value1");
        assertEquals("value1", underTest.invoke(testObject, testMethod, null));
        assertEquals("value1", underTest.invoke(testObject, testMethod, null));
        verify(invocationHandler, times(1)).invoke(testObject, testMethod, null);
    }

    @Test
    public void testCacheHitNull() throws Throwable {
        when(invocationHandler.invoke(testObject, testMethod, null)).thenReturn(null);
        assertNull(underTest.invoke(testObject, testMethod, null));
        assertNull(underTest.invoke(testObject, testMethod, null));
        verify(invocationHandler, times(1)).invoke(testObject, testMethod, null);
    }
}
