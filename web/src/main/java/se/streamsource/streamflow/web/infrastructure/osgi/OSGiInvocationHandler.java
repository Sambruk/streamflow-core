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

import org.osgi.util.tracker.ServiceTracker;
import org.qi4j.api.service.ServiceImporterException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
* JAVADOC
*/
class OSGiInvocationHandler
   implements InvocationHandler
{
   private ServiceTracker serviceTracker;

   OSGiInvocationHandler( ServiceTracker serviceTracker )
   {
      this.serviceTracker = serviceTracker;
   }

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      Object instance = getInstance();

      if (instance == null)
         throw new ServiceImporterException("OSGi service is currently not available");

      return method.invoke( instance, args );
   }

   public Object getInstance()
   {
      try
      {
         return serviceTracker.waitForService( 5000 );
      } catch (InterruptedException e)
      {
         // No such service found
         return null;
      }
   }

   public boolean isActive()
   {
      return serviceTracker.size() > 0;
   }
}
