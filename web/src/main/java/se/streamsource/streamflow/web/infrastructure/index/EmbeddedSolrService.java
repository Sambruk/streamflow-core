package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;

@Mixins(EmbeddedSolrService.EmbeddedSolrServiceMixin.class)
public interface EmbeddedSolrService extends Activatable, ServiceComposite
{
   class EmbeddedSolrServiceMixin implements Activatable
   {
      @Service
      FileConfiguration fileConfig;

      public void activate() throws Exception
      {
         // TODO: Fix loading of config through classpath
/*
         File directory = new File( fileConfig.dataDirectory() + "/solr" );
         directory.mkdir();

         System.setProperty( "solr.solr.home", directory.getAbsolutePath() );

         CoreContainer.Initializer initializer = new CoreContainer.Initializer();
         CoreContainer coreContainer = initializer.initialize();
         EmbeddedSolrServer server = new EmbeddedSolrServer( coreContainer, "" );
*/
      }

      public void passivate() throws Exception
      {
         //To change body of implemented methods use File | Settings | File Templates.
      }
   }
}
