package org.apache.sling.caconfig.impl.def;

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
import org.mockito.runners.MockitoJUnitRunner;

import javax.script.Bindings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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
}