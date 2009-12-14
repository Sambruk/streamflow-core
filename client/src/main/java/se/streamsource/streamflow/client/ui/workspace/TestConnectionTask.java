/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.LoggerCategories;
import se.streamsource.streamflow.client.ui.administration.AccountModel;

import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class TestConnectionTask extends Task<String, Void>
{
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
      Logger.getLogger( LoggerCategories.STATUS ).info( "Testing connection..." );
      message( "Testing connection" );
      Logger.getLogger( LoggerCategories.PROGRESS ).info( "50/100" );
      try
      {
         return model.test();
      } finally
      {
         Logger.getLogger( LoggerCategories.PROGRESS ).info( "100/100" );
         Logger.getLogger( LoggerCategories.STATUS ).info( "Ready" );
      }
   }
}
