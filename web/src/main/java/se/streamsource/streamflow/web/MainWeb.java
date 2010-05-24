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

package se.streamsource.streamflow.web;

import org.apache.log4j.xml.DOMConfigurator;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.rest.StreamFlowRestApplication;

import java.net.URL;

/**
 * JAVADOC
 */
public class MainWeb
{
   private Component component;
   public StreamFlowRestApplication application;
   public Logger logger;

   public static void main( String[] args ) throws Exception
   {
      new MainWeb().start();
   }

   public void start() throws Exception
   {
/*
      // Remove default handlers - we do our own logging!
      for (Handler handler : Logger.getLogger( "" ).getHandlers())
      {
         Logger.getLogger( "" ).removeHandler( handler );
      }

      // Install SL4J Bridge. This will eventually delegate to log4j for logging
      SLF4JBridgeHandler.install();
*/

      URL logConfig = getClass().getResource( "/log4j.xml" );
      DOMConfigurator.configure( logConfig );

      logger = LoggerFactory.getLogger( getClass() );
      logger.info("Starting Streamflow");
      logger.info( "Classloader current:"+Thread.currentThread().getContextClassLoader()+" class:"+getClass().getClassLoader() );

      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

      try
      {
         component = new Component();
         component.getClients().add( Protocol.CLAP );
         component.getClients().add( Protocol.FILE );
         application = new StreamFlowRestApplication( component.getContext().createChildContext() );

         // TODO Could we make it available some other way?
         component.getDefaultHost().attach( "/streamflow/streamflow", application );
         component.start();
         logger.info("Started Streamflow");
      } catch (Exception e)
      {
         logger.info("Could not start Streamflow");

         if (component != null)
            component.stop();
         throw e;
      } finally
      {
         Thread.currentThread().setContextClassLoader( null );
      }
   }

   public Restlet getApplication()
   {
      // Set context classloader so that resources are taken from this bundle
      return new Filter(application.getContext(), application)
      {
         @Override
         protected int doHandle( Request request, Response response )
         {
            Thread thread = Thread.currentThread();
            ClassLoader oldCL = thread.getContextClassLoader();
            thread.setContextClassLoader(getClass().getClassLoader() );

            try
            {
               return super.doHandle( request, response );
            } finally
            {
               thread.setContextClassLoader( oldCL );
            }
         }
      };
   }

   public void stop() throws Exception
   {
      logger.info("Stopping Streamflow");
      component.stop();
      logger.info("Stopped Streamflow");
   }
}
