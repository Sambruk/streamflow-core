/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.infrastructure.caching;

import net.sf.ehcache.*;

/**
 * Utility methods for working with caches. Handles the case where no caching is available.
 */
public class Caching
{
   CachingService caching;
   private Caches cache;

   public Caching( CachingService caching, Caches cache )
   {
      this.caching = caching;
      this.cache = cache;
   }

   /**
    * Increase a cached value identified by "id" with "increment".
    * If there is no currently cached value, do nothing.
    *
    * @param id id of the element
    * @param increment how much to increment the value. May be negative for subtraction.
    */
   public void addToCache( String id, long increment)
   {
      if (caching != null)
      {
         Ehcache ehcache = caching.manager().getEhcache( cache.name() );
         Element element = ehcache.get( id );
         if (element != null)
         {
            element = new Element(id, Long.toString(Long.parseLong((String) element.getValue())+increment));
            ehcache.put( element );
         }
      }
   }

   public void invalidateCache( String id )
   {
      if (caching != null)
      {
         caching.manager().getEhcache( cache.name() ).remove( id );
      }
   }

   public void put(Element element)
   {
      if (caching != null)
      {
         caching.manager().getEhcache( cache.name() ).put( element );
      }
   }

   public Element get(Object key)
   {
      if (caching != null)
      {
         CacheManager cacheManager = caching.manager();
         Ehcache ehcache = cacheManager.getEhcache( cache.name() );
         return ehcache.get( key );
      } else
      {
         return null;
      }
   }
}
