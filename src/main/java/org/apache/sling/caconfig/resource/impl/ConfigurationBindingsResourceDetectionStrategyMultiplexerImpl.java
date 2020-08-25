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

import java.util.Map;
import javax.script.Bindings;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationBindingsResourceDetectionStrategyMultiplexer;
import org.apache.sling.caconfig.resource.spi.ConfigurationBindingsResourceDetectionStrategy;
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
 * Detects all {@link ConfigurationBindingsResourceDetectionStrategy} implementations in the container
 * and consolidates their result based on service ranking.
 */
@Component(service = ConfigurationBindingsResourceDetectionStrategyMultiplexer.class,
    reference={
        @Reference(name="configurationBindingsResourceDetectionStrategy", service= ConfigurationBindingsResourceDetectionStrategy.class,
            bind="bindConfigurationBindingsResourceDetectionStrategy", unbind="unbindConfigurationBindingsResourceDetectionStrategy",
            cardinality=ReferenceCardinality.MULTIPLE,
            policy=ReferencePolicy.DYNAMIC, policyOption=ReferencePolicyOption.GREEDY)
    })
public class ConfigurationBindingsResourceDetectionStrategyMultiplexerImpl implements ConfigurationBindingsResourceDetectionStrategyMultiplexer {

    private RankedServices<ConfigurationBindingsResourceDetectionStrategy> resourceDetectionStrategies = new RankedServices<>(Order.DESCENDING);

    protected void bindConfigurationBindingsResourceDetectionStrategy(ConfigurationBindingsResourceDetectionStrategy item, Map<String, Object> props) {
        resourceDetectionStrategies.bind(item, props);
    }

    protected void unbindConfigurationBindingsResourceDetectionStrategy(ConfigurationBindingsResourceDetectionStrategy item, Map<String, Object> props) {
        resourceDetectionStrategies.unbind(item, props);
    }

    /**
     * Detects the resource by looking at the available bindings from the first implementation that has an answer.
     */
    @Override
    public @Nullable Resource detectResource(@NotNull Bindings bindings) {
        for (ConfigurationBindingsResourceDetectionStrategy resourceDetectionStrategy : resourceDetectionStrategies) {
            Resource resource = resourceDetectionStrategy.detectResource(bindings);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }
}
