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

import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationInjectResourceDetectionStrategyMultiplexer;
import org.apache.sling.caconfig.spi.ConfigurationInjectResourceDetectionStrategy;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Detects all {@link ConfigurationInjectResourceDetectionStrategy} implementations in the container
 * and consolidates their result based on service ranking.
 */
@Component(service = ConfigurationInjectResourceDetectionStrategyMultiplexer.class,
    reference={
        @Reference(name="configurationBindingsResourceDetectionStrategy", service=ConfigurationInjectResourceDetectionStrategy.class,
            bind="bindConfigurationInjectResourceDetectionStrategy", unbind="unbindConfigurationInjectResourceDetectionStrategy",
            cardinality=ReferenceCardinality.MULTIPLE,
            policy=ReferencePolicy.DYNAMIC, policyOption=ReferencePolicyOption.GREEDY)
    })
public class ConfigurationInjectResourceDetectionStrategyMultiplexerImpl implements ConfigurationInjectResourceDetectionStrategyMultiplexer {

    private RankedServices<ConfigurationInjectResourceDetectionStrategy> resourceDetectionStrategies = new RankedServices<>(Order.DESCENDING);

    protected void bindConfigurationInjectResourceDetectionStrategy(ConfigurationInjectResourceDetectionStrategy item, Map<String, Object> props) {
        resourceDetectionStrategies.bind(item, props);
    }

    protected void unbindConfigurationInjectResourceDetectionStrategy(ConfigurationInjectResourceDetectionStrategy item, Map<String, Object> props) {
        resourceDetectionStrategies.unbind(item, props);
    }

    /**
     * Detects the resource by looking at the available bindings from the first implementation that has an answer.
     */
    @Override
    public @Nullable Resource detectResource(@NotNull SlingHttpServletRequest request) {
        for (ConfigurationInjectResourceDetectionStrategy resourceDetectionStrategy : resourceDetectionStrategies) {
            Resource resource = resourceDetectionStrategy.detectResource(request);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

}
