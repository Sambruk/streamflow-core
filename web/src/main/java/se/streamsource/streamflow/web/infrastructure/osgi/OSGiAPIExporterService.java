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
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.Qi4j;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import java.util.Properties;

/**
* JAVADOC
*/
@Mixins(OSGiAPIExporterService.Mixin.class)
public interface OSGiAPIExporterService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      @Structure
      Qi4j api;

      ServiceRegistration registration;

      public void activate() throws Exception
      {
         BundleContext context = BundleReference.class.cast( OSGiAPIExporterService.class.getClassLoader() ).getBundle().getBundleContext();

         // Export Qi4j API
         registration = context.registerService( Qi4j.class.getName(), api, new Properties() );
      }

      public void passivate() throws Exception
      {
         registration.unregister();
         registration = null;
      }
   }
}
