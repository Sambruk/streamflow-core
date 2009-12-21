package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.params.SolrParams;
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

/*
         SolrInputDocument doc = new SolrInputDocument();
         doc.setField( "id", "123" );
         doc.setField( "text", "Hello World" );

         server.add( doc );
         server.commit();

         NamedList list = new NamedList();
         list.add("q", "world");
         QueryResponse query = server.query( SolrParams.toSolrParams(list ));
         SolrDocumentList results = query.getResults();
         for (SolrDocument result : results)
         {
            System.out.println(result.getFirstValue( "id" ));
         }
*/
      }

      public void passivate() throws Exception
      {
         coreContainer.shutdown();
      }
   }
} 
