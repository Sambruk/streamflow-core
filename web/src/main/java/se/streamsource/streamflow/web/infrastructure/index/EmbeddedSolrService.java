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

package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.embedded.*;
import org.apache.solr.core.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import se.streamsource.streamflow.infrastructure.configuration.*;

import java.io.*;
import java.lang.reflect.*;

@Mixins(EmbeddedSolrService.EmbeddedSolrServiceMixin.class)
public interface EmbeddedSolrService extends Activatable, ServiceComposite
{
   public SolrServer getSolrServer();

   public SolrCore getSolrCore();

   abstract class EmbeddedSolrServiceMixin
         implements Activatable,EmbeddedSolrService
   {
      @Service
      FileConfiguration fileConfig;
      public CoreContainer coreContainer;
      public EmbeddedSolrServer server;

      private SolrCore core;

      public void activate() throws Exception
      {
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         try
         {
            File directory = new File( fileConfig.dataDirectory() + "/solr" );
            directory.mkdir();


            System.setProperty( "solr.solr.home", directory.getAbsolutePath() );

            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer( coreContainer, "" );
            core = coreContainer.getCore( "" );
         } finally
         {
            Thread.currentThread().setContextClassLoader( oldCl );
         }
      }

      public void passivate() throws Exception
      {
         core.closeSearcher();
         coreContainer.shutdown();

         // Clear instance fields for GC purposes
         Field instanceField = SolrCore.class.getDeclaredField( "instance" );
         instanceField.setAccessible( true );
         instanceField.set( null, null );

         SolrConfig.config = null;
      }

      public SolrServer getSolrServer()
      {
         return server;
      }

      public SolrCore getSolrCore()
      {
         return core;
      }
   }
}
