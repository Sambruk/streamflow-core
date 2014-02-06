/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import net.sf.ehcache.CacheManager;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

/**
 * Service wrapper for Ehcache. Manages a CacheManager instance.
 */
@Mixins(CachingServiceComposite.Mixin.class)
public interface CachingServiceComposite
   extends CachingService, ServiceComposite, Activatable
{
   class Mixin
      implements CachingService, Activatable
   {
      public Mixin()
      {
         System.out.println("New caching:"+this);
      }

      CacheManager manager;

      public void activate() throws Exception
      {
         manager = new CacheManager(getClass().getResource( "/ehcache.xml" ));
         System.out.println("Activated caching:"+manager);
      }

      public void passivate() throws Exception
      {
         manager.shutdown();
      }

      public CacheManager manager()
      {
         return manager;
      }
   }
}
