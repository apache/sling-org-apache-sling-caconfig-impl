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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.impl.models.CAConfigViaProviderCacheProvider;
import org.apache.sling.caconfig.models.via.ContextAwareConfigResource;
import org.apache.sling.models.annotations.ViaProviderType;
import org.apache.sling.models.spi.ViaProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component(immediate = true, service = ViaProvider.class)
public class CAConfigViaProvider implements ViaProvider {

    @Reference
    private ConfigurationResolver configurationResolver;

    @Reference
    private CAConfigViaProviderCacheProvider cacheProvider;

    @Override
    public Class<? extends ViaProviderType> getType() {
        return ContextAwareConfigResource.class;
    }

    @Override
    public Object getAdaptable(Object original, String value) {


        Resource resource = getResource(original);

        if (isBlank(value) || resource == null) {
            return ORIGINAL;
        }

        if(cacheProvider.contains(resource.getPath())){
            return cacheProvider.get(resource.getPath());
        }

        ConfigurationBuilder config = configurationResolver.get(resource);
        Resource adaptable = config.name(value).asAdaptable(Resource.class);

        cacheProvider.put(resource.getPath(), adaptable);

        return adaptable;
    }

    private Resource getResource(Object adaptable){
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        if(adaptable instanceof Adaptable){
            return ((Adaptable) adaptable).adaptTo(Resource.class);
        }
        return null;
    }
}
