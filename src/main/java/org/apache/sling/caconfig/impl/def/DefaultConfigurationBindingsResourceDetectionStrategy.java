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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.caconfig.resource.spi.ConfigurationBindingsResourceDetectionStrategy;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.script.Bindings;

@Component(
        service = ConfigurationBindingsResourceDetectionStrategy.class,
        property = {
                Constants.SERVICE_RANKING + "=100"
        }
)
public class DefaultConfigurationBindingsResourceDetectionStrategy implements ConfigurationBindingsResourceDetectionStrategy {
    @Override
    public @Nullable Resource detectResource(final Bindings bindings) {
        if (bindings.containsKey(SlingBindings.REQUEST)) {
            SlingHttpServletRequest request = (SlingHttpServletRequest) bindings.get(SlingBindings.REQUEST);
            return request.getResource();
        }
        return null;
    }
}
