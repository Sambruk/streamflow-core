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

package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;

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

      public void activate() throws Exception
      {
         File directory = new File( fileConfig.dataDirectory() + "/solr" );
         directory.mkdir();



         System.setProperty( "solr.solr.home", directory.getAbsolutePath() );

         CoreContainer.Initializer initializer = new CoreContainer.Initializer();
         coreContainer = initializer.initialize();
         server = new EmbeddedSolrServer( coreContainer, "" );
      }

      public void passivate() throws Exception
      {
         coreContainer.shutdown();
      }

      public SolrServer getSolrServer()
      {
         return server;
      }

      public SolrCore getSolrCore()
      {
         return coreContainer.getCore( "" );
      }
   }
}
