/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
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
