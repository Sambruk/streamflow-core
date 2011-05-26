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

package se.streamsource.streamflow.web;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.engine.http.HttpResponse;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.rest.StreamflowRestApplication;

/**
 * JAVADOC
 */
public class MainWeb
{
   private Component component;
   public StreamflowRestApplication application;
   public Logger logger;

   public static void main(String[] args) throws Throwable
   {
      new MainWeb().start();
   }

   public void start() throws Throwable
   {
/*
      // Remove default handlers - we do our own logging!
      for (Handler handler : Logger.getLogger( "" ).getHandlers())
      {
         Logger.getLogger( "" ).removeHandler( handler );
      }

      // Install SL4J Bridge. This will eventually delegate to log4j for logging
      SLF4JBridgeHandler.install();

      URL logConfig = getClass().getResource( "/log4j.xml" );
      DOMConfigurator.configure( logConfig );
*/

      logger = LoggerFactory.getLogger(getClass());
      logger.info("Starting Streamflow");

      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

      try
      {
         component = new Component();
         component.getClients().add(Protocol.CLAP);
         component.getClients().add(Protocol.FILE);
         component.getClients().add(Protocol.HTTP);


         application = new StreamflowRestApplication(component.getContext().createChildContext());
         component.getDefaultHost().attach("/streamflow", application);

/*
         VirtualHost virtualHost = new VirtualHost(component.getContext());
         virtualHost.setHostDomain("jayway.local");
         virtualHost.getContext().getAttributes().put("streamflow.host", virtualHost.getHostDomain());
         Application virtualApplication = new StreamflowRestApplication(virtualHost.getContext());
         virtualHost.attach("/streamflow", virtualApplication);
         component.getHosts().add(virtualHost);
*/

         component.start();
         logger.info("Started Streamflow");
      } catch (Throwable e)
      {
         logger.info("Could not start Streamflow", e);

         if (component != null)
            component.stop();
         throw e;
      } finally
      {
         Thread.currentThread().setContextClassLoader(null);
      }
   }

   public Restlet getApplication()
   {
      // Set roleMap classloader so that resources are taken from this bundle
      return new ClassLoaderFilter(application.getContext(), component);
   }

   public void stop() throws Throwable
   {
      logger.info("Stopping Streamflow");
      component.stop();
      logger.info("Stopped Streamflow");
   }

   static class ClassLoaderFilter
           extends Filter
   {
      private ClassLoader cl;

      ClassLoaderFilter(Context context, Restlet next)
      {
         super(context, next);

         cl = getClass().getClassLoader();
      }

      @Override
      protected int doHandle(Request request, Response response)
      {
         Thread thread = Thread.currentThread();
         ClassLoader oldCL = thread.getContextClassLoader();
         thread.setContextClassLoader(cl);

         try
         {
            return super.doHandle(request, response);
         } finally
         {
            HttpResponse.setCurrent(null);
            Context.setCurrent(null);
            Application.setCurrent(null);

            thread.setContextClassLoader(oldCL);
         }
      }
   }
}
