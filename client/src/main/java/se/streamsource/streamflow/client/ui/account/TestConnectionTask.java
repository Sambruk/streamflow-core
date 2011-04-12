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

package se.streamsource.streamflow.client.ui.account;

import org.jdesktop.application.Application;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.restlet.*;
import org.slf4j.*;
import se.streamsource.streamflow.client.*;

/**
 * JAVADOC
 */
public class TestConnectionTask extends Task<String, Void>
{
   final Logger statusLogger = LoggerFactory.getLogger( LoggerCategories.STATUS );
   final Logger progressLogger = LoggerFactory.getLogger( LoggerCategories.PROGRESS );
   @Uses
   AccountModel model;

   @Service
   Restlet client;

   public TestConnectionTask( @Service Application app )
   {
      super( app );
   }

   protected String doInBackground() throws Exception
   {
      statusLogger.info( "Testing connection..." );
      message( "Testing connection" );
      progressLogger.info( "50/100" );
      try
      {
         return model.test();
      } finally
      {
         progressLogger.info( "100/100" );
         statusLogger.info( "Ready" );
      }
   }
}
