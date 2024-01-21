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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationResourceResolvingStrategyMultiplexer;
import org.apache.sling.caconfig.management.multiplexer.ContextPathStrategyMultiplexer;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.resource.impl.util.ConfigNameUtil;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service=ConfigurationResourceResolver.class, immediate=true)
@Designate(ocd=ConfigurationResourceResolverImpl.Config.class)
public class ConfigurationResourceResolverImpl implements ConfigurationResourceResolver {
    
    
    @ObjectClassDefinition
    public static @interface Config {
        
        @AttributeDefinition(name="enable caching", description="Enable caching of resolved results per ResourceResolver")
        boolean enableCaching() default false;
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationResourceResolverImpl.class);
    private static final String MAP_KEY = ConfigurationResourceResolverImpl.class.getName() + "_Cache";
    
    @Reference
    private ContextPathStrategyMultiplexer contextPathStrategy;
    @Reference
    private ConfigurationResourceResolvingStrategyMultiplexer configurationResourceResolvingStrategy;

    private boolean enableCaching;
    
    @Activate
    @Modified
    public void modified(Config config) {
        enableCaching = config.enableCaching();
        LOG.debug("CaConfig caching = {}", enableCaching);
    }
    
    @Override
    public Resource getResource(@NotNull Resource resource, @NotNull String bucketName, @NotNull String configName) {
        ConfigNameUtil.ensureValidConfigName(configName);
        
        Resource cacheResult = getResourceFromCache(resource, bucketName, configName);
        if (cacheResult != null) {
            LOG.trace("getResource (cached) resource={}, bucketName={}, configName={}",resource.getPath(),bucketName, configName);
            return cacheResult;
        }
        
        Resource result = configurationResourceResolvingStrategy.getResource(resource, Collections.singleton(bucketName), configName);
        LOG.trace("getResource (resolved) resource={}, bucketName={}, configName={}",resource.getPath(),bucketName, configName);
        putResourceToCache(resource, bucketName, configName, result);
        return result;
    }

    @Override
    public @NotNull Collection<Resource> getResourceCollection(@NotNull Resource resource, @NotNull String bucketName, @NotNull String configName) {
        ConfigNameUtil.ensureValidConfigName(configName);
        
        Collection<Resource> cacheResult = getCollectionFromCache(resource,bucketName,configName);
        if (cacheResult != null) {
            LOG.trace("getResourceCollection (cached) resource={}, bucketName={}, configName={}",resource.getPath(),bucketName, configName);
            return cacheResult;
        }
        
        Collection<Resource> result = configurationResourceResolvingStrategy.getResourceCollection(resource, Collections.singleton(bucketName), configName);
        if (result == null) {
            result = Collections.emptyList();
        }
        LOG.trace("getResourceCollection (resolved) resource={}, bucketName={}, configName={}",resource.getPath(),bucketName, configName);
        putCollectionToCache(resource, bucketName, configName, result);
        return result;
    }

    @Override
    public String getContextPath(@NotNull Resource resource) {
        LOG.trace("getContextPath resource={}", resource.getPath());
        Iterator<ContextResource> it = contextPathStrategy.findContextResources(resource);
        if (it.hasNext()) {
            return it.next().getResource().getPath();
        }
        else {
            return null;
        }
    }

    @Override
    public @NotNull Collection<String> getAllContextPaths(@NotNull Resource resource) {
        LOG.trace("getAllContextPaths resource={}", resource.getPath());
        final List<String> contextPaths = new ArrayList<>();
        Iterator<ContextResource> contextResources = contextPathStrategy.findContextResources(resource);
        while (contextResources.hasNext()) {
            contextPaths.add(contextResources.next().getResource().getPath());
        }
        return contextPaths;
    }
    
    
    /**
     * Caching implementation
     * 
     * The cache is not threadsafe (as the resourceResolver is not threadsafe as well), thus a naive
     * implementation should suffice.
     * 
     */
  
    // Calculate the cache key for a caconfig entry
    private String createCaConfigCacheKey(Resource resource, String bucketName, String configName) {
        return resource.getPath() + "--" + bucketName + "--" + configName;
    }
    
    // Retrieve the caconfig map from the ResourceResolver (and create it if required)
    private Map<String,Object> getCacheMap(@NotNull Resource resource) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        if (resourceResolver.getPropertyMap().containsKey(MAP_KEY)) {
            return (Map<String,Object>) resourceResolver.getPropertyMap().get(MAP_KEY);
        } else {
            Map<String,Object> map = new HashMap<>();
            resourceResolver.getPropertyMap().put(MAP_KEY, map);
            return map;
        }
    }
    
    /**
     * Retrieves a result from the cache
     * @param resource the resource
     * @param bucketName the bucketName
     * @param configName the configName
     * @return the cache result or null
     */
    @SuppressWarnings("unchecked")
    private @Nullable Collection<Resource> getCollectionFromCache(Resource resource, String bucketName, String configName) {
        if (!enableCaching) {
            return null;
        }
        String cacheKey = "collection--" + createCaConfigCacheKey(resource, bucketName, configName);
        return (Collection<Resource>) getCacheMap(resource).get(cacheKey);
    }
    
    private void putCollectionToCache(Resource resource, String bucketName, String configName, Collection<Resource> value) {
        String cacheKey = "collection--" + createCaConfigCacheKey(resource, bucketName, configName);
        getCacheMap(resource).put(cacheKey, value);
    }
    
    private @Nullable Resource getResourceFromCache(Resource resource, String bucketName, String configName) {
        if (!enableCaching) {
            return null;
        }
        String cacheKey = "resource--" + createCaConfigCacheKey(resource, bucketName, configName);
        return (Resource) getCacheMap(resource).get(cacheKey);
    }
    
    private void putResourceToCache(Resource resource, String bucketName, String configName, Resource value) {
        String cacheKey = "resource--" + createCaConfigCacheKey(resource, bucketName, configName);
        getCacheMap(resource).put(cacheKey, value);
    }

}
