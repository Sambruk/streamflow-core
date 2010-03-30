/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.bridge.SLF4JBridgeHandler;
import se.streamsource.streamflow.web.rest.StreamFlowRestApplication;

import java.util.logging.Logger;
import java.util.logging.Handler;

/**
 * JAVADOC
 */
public class MainWeb
{
   private Component component;

   public static void main( String[] args ) throws Exception
   {
      new MainWeb().start();
   }

   public void start() throws Exception
   {
      // Remove default handlers - we do our own logging!
      for (Handler handler : Logger.getLogger( "" ).getHandlers())
      {
         Logger.getLogger( "" ).removeHandler( handler );
      }

      // Install SL4J Bridge. This will eventually delegate to log4j for logging
      SLF4JBridgeHandler.install();

      component = new Component();
      component.getServers().add( Protocol.HTTP, 8040 );
      component.getClients().add( Protocol.CLAP );
      component.getClients().add( Protocol.FILE );
      StreamFlowRestApplication application = new StreamFlowRestApplication( component.getContext().createChildContext() );
      component.getDefaultHost().attach( "/streamflow/streamflow", application );
      component.start();
   }

   public void stop() throws Exception
   {
      component.stop();
   }
}
