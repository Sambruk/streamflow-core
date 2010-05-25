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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.restlet.Restlet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
* Import a service from OSGi and expose in the Qi4j application.
*/
public class OSGiServiceImporter
      implements ServiceImporter
{
   @Service
   OSGiServices services;

   public Object importService( final ImportedServiceDescriptor serviceDescriptor ) throws ServiceImporterException
   {
      final ServiceTracker serviceTracker = services.getServiceTracker( serviceDescriptor.type().getName() );

      OSGiInvocationHandler handler = new OSGiInvocationHandler(serviceTracker);
      Object proxy = Proxy.newProxyInstance( serviceDescriptor.type().getClassLoader(),
            new Class[]{serviceDescriptor.type()},
            handler);
      return proxy;
   }

   public boolean isActive( Object instance )
   {
      OSGiInvocationHandler handler = (OSGiInvocationHandler) Proxy.getInvocationHandler( instance );
      return handler.isActive();
   }

   static class OSGiInvocationHandler
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
}
