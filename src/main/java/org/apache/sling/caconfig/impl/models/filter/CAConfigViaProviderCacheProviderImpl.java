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
package org.apache.sling.caconfig.impl.models.filter;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.impl.models.CAConfigViaProviderCacheProvider;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(
    service = {CAConfigViaProviderCacheProvider.class,Filter.class},
    property = {
        "sling.filter.order=" + Integer.MIN_VALUE,
        "sling.filter.scope=REQUEST"
    }
)
public class CAConfigViaProviderCacheProviderImpl implements CAConfigViaProviderCacheProvider, Filter {

    private static final ThreadLocal<Map<String,Resource>> THREAD_LOCAL = new ThreadLocal<Map<String,Resource>>() {
        @Override protected Map<String,Resource> initialValue() {
             set(new HashMap<String,Resource>());
             return get();
        }
    };


    @Override
    public boolean contains(String path) {
        return THREAD_LOCAL.get().containsKey(path);
    }

    @Override
    public Resource get(String path) {
        return THREAD_LOCAL.get().get(path);
    }

    @Override
    public void put(String path, Resource adapted) {
        THREAD_LOCAL.get().put(path, adapted);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Since we are in the context of a SlingFilter; the Request is always a SlingHttpServletRequest
        try {
            // Continue processing the request chain
            chain.doFilter(request, response);
        } finally {
            // Good housekeeping; Clean up after yourself!!!
            THREAD_LOCAL.remove();
        }
    }

    @Override
    public void destroy() {

    }
}
