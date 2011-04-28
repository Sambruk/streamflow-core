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

package se.streamsource.dci.test;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * JAVADOC
 */
public class TestWebApp
{
   private Component component;

   public static void main( String[] args ) throws Exception
   {
      new TestWebApp().start();
   }

   public void start() throws Exception
   {
      // Install SL4J Bridge. This will eventually delegate to log4j for logging
      SLF4JBridgeHandler.install();

      org.apache.log4j.Logger.getRootLogger().addAppender( new ConsoleAppender(new PatternLayout("%c{0} %n%m")) );

      component = new Component();
      component.getServers().add( Protocol.HTTP, 8080 );
      component.getClients().add( Protocol.CLAP );
      component.getClients().add( Protocol.FILE );
      TestRestletApplication application = new TestRestletApplication( component.getContext().createChildContext() );
      component.getDefaultHost().attach( application );
      component.start();
   }

   public void stop() throws Exception
   {
      component.stop();
   }
}
