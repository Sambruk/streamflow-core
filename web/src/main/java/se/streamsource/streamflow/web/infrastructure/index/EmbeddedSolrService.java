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
