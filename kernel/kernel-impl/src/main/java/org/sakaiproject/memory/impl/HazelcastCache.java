/******************************************************************************
 * $URL$
 * $Id$
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.memory.impl;

import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.CacheLoader;
import org.sakaiproject.memory.api.CacheStatistics;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Contains Hazelcast implementation related to a HC Map based cache.
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class HazelcastCache extends BasicMapCache {
    final Log log = LogFactory.getLog(HazelcastCache.class);

    IMap<String, ?> cache;

    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param hcMap the hazelcast Map (IMap)
     */
    public HazelcastCache(IMap hcMap) {
        super(hcMap.getName(), hcMap);
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public void registerCacheEventListener(org.sakaiproject.memory.api.CacheEventListener cacheEventListener) {
        this.cacheEventListener = cacheEventListener;
    }

    @Override
    public String getDescription() {
        return "HCMap("+getName()+"):"+cache.getLocalMapStats(); // TODO we really want the cluster stats
    }

    @Override
    public void attachLoader(CacheLoader cacheLoader) {
        this.loader = cacheLoader;
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        return new CacheStatistics() {
            @Override
            public long getCacheHits() {
                return 0;
            } // TODO get real numbers
            @Override
            public long getCacheMisses() {
                return 0;
            } // TODO get real numbers
        };
    }

    @Override
    public Properties getProperties(boolean includeExpensiveDetails) {
        Properties p = new Properties();
        p.put("name", getName());
        p.put("class", this.getClass().getSimpleName());
        // TODO fill in more info about the cache (like stats)
        return p;
    }

    // BULK operations - KNL-1246

    @Override
    public Map<String, Object> getAll(Set<String> keys) {
        //noinspection unchecked
        return (Map) cache.getAll(keys);
    }

    @Override
    public void putAll(Map<String, Object> map) {
        //noinspection unchecked
        cache.putAll((Map)map);
    }

    @Override
    public void removeAll(Set<String> keys) {
        if (!keys.isEmpty()) {
            for (String key : keys) {
                if (key == null) {
                    throw new NullPointerException("keys Set for removeAll cannot contain nulls (but it does)");
                }
                cache.remove(key);
            }
        }
    }

    @Override
    public void close() {
        this.cache.destroy();
    }


    // **************************************************************************
    // DEPRECATED methods - REMOVE THESE
    // **************************************************************************

    /**
     * @deprecated REMOVE THIS
     */
    public void destroy() {
        this.close();
    }

    /**
     * @deprecated REMOVE THIS
     */
    public void put(Object key, Object payload, int duration) {
        put(String.valueOf(key), payload);
    }

}
