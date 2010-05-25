/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.infrastructure.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(OSGiServicesService.Mixin.class)
public interface OSGiServicesService
   extends ServiceComposite, OSGiServices
{
   class Mixin
      implements Activatable, OSGiServices
   {
      @Structure
      Application application;

      private BundleContext bundleContext;
      private Map<String, ServiceTracker> trackers = new HashMap<String, ServiceTracker>( );

      public void activate() throws Exception
      {
         bundleContext = application.metaInfo( BundleContext.class );
      }

      public void passivate() throws Exception
      {
         for (ServiceTracker serviceTracker : trackers.values())
         {
            serviceTracker.close();
         }
         trackers.clear();
      }

      public synchronized ServiceTracker getServiceTracker( String type )
      {
         ServiceTracker tracker = trackers.get(type);
         if (tracker == null)
         {
            tracker = new ServiceTracker(bundleContext, type, null);
            tracker.open();
            trackers.put(type, tracker);
         }

         return tracker;
      }
   }
}
