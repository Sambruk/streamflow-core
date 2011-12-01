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

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;
import java.lang.reflect.Field;

@Mixins(EmbeddedSolrService.EmbeddedSolrServiceMixin.class)
public interface EmbeddedSolrService extends Activatable, ServiceComposite
{
   public SolrServer getSolrServer( String name );

   public SolrCore getSolrCore( String name );

   abstract class EmbeddedSolrServiceMixin
         implements Activatable,EmbeddedSolrService
   {
      @Service
      FileConfiguration fileConfig;
      public CoreContainer coreContainer;
      public EmbeddedSolrServer coreServer;
      public EmbeddedSolrServer streetServer;

      private SolrCore core;

      public void activate() throws Exception
      {
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         try
         {
            File directory = new File( fileConfig.dataDirectory() + "/solr" );
            if( directory.mkdir() || !new File( directory + "/solr.xml").exists() )
            {

               // multicore solr.xml
               Inputs.text( Thread.currentThread().getContextClassLoader().getResource( "solr.xml" ) )
                     .transferTo( Outputs.text( new File( directory.getAbsolutePath() + "/solr.xml" ) ) );

            }
            System.setProperty( "solr.solr.home", directory.getAbsolutePath() );

            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            coreContainer = initializer.initialize();
            coreServer = new EmbeddedSolrServer( coreContainer, "sf-core" );
            streetServer = new EmbeddedSolrServer( coreContainer, "sf-streetcache" );

            core = coreContainer.getCore( "sf-core" );
         } finally
         {
            Thread.currentThread().setContextClassLoader( oldCl );
         }
      }

      public void passivate() throws Exception
      {
         for( SolrCore core : coreContainer.getCores())
         {
            core.closeSearcher();
         }
         coreContainer.shutdown();

         // Clear instance fields for GC purposes
         Field instanceField = SolrCore.class.getDeclaredField( "instance" );
         instanceField.setAccessible( true );
         instanceField.set( null, null );

         SolrConfig.config = null;
      }

      public SolrServer getSolrServer( String name)
      {
         return "sf-core".equals( name ) ? coreServer : streetServer;
      }

      public SolrCore getSolrCore( String name )
      {
         return coreContainer.getCore(  name  );
      }
   }
}
