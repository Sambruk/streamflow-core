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

package se.streamsource.streamflow.web.infrastructure.plugin.address;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.restlet.client.CommandQueryClientFactory;
import se.streamsource.dci.restlet.client.NullResponseHandler;
import se.streamsource.streamflow.server.plugin.address.StreetAddressLookup;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrService;
import se.streamsource.streamflow.web.infrastructure.plugin.StreetAddressLookupConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that looks up street addresses in a REST plugin
 */
@Mixins(StreetAddressLookupService.Mixin.class)
public interface StreetAddressLookupService
      extends ServiceComposite, StreetAddressLookup, Configuration, Activatable
{
   class Mixin
         implements StreetAddressLookup, Activatable
   {
      @Service
      private EmbeddedSolrService solr;

      @This
      Configuration<StreetAddressLookupConfiguration> config;

      @Structure
      Module module;

      private CommandQueryClient cqc;

      private SolrCore core;

      Logger log = LoggerFactory.getLogger( StreetAddressLookupService.class );

      public void activate() throws Exception
      {
         config.configuration();

         if (config.configuration().enabled().get())
         {
            Reference serverRef = new Reference( config.configuration().url().get() );
            Client client = new Client( Protocol.HTTP );
            client.start();

            cqc = module.objectBuilderFactory().newObjectBuilder(CommandQueryClientFactory.class).use( client, new NullResponseHandler() ).newInstance().newClient( serverRef );


            core = solr.getSolrCore( "sf-streetcache" );

            if( config.configuration().forceReload().get() ||
                  config.configuration().lastLoaded().get() == 0 ||
                  ( (config.configuration().lastLoaded().get() - System.currentTimeMillis() )
                        > config.configuration().loadFrequence().get() ) )
            {
               reindex();
               core.close();

               if( config.configuration().forceReload().get())
               {
                  config.configuration().forceReload().set( false );
                  config.save();
               }
            }
         }
      }

      public void passivate() throws Exception
      {
      }

      public StreetList lookup(StreetValue streetTemplate)
      {
         if( (config.configuration().lastLoaded().get() - System.currentTimeMillis() )
                        > config.configuration().loadFrequence().get() )
         {
            reindex();
         }

         ValueBuilder<StreetList> streetListBuilder = module.valueBuilderFactory().newValueBuilder( StreetList.class );

         try
         {

            int limit = config.configuration().limit().get();
            if (config.configuration().minkeywordlength().get() <= streetTemplate.address().get().length())
            {

               NamedList list = new NamedList();

               list.add( "q", "address:" + streetTemplate.address().get().trim().replace( " ", "*" ) + "*" );

               QueryResponse query = solr.getSolrServer( "sf-streetcache" ).query( SolrParams.toSolrParams( list ) );
               SolrDocumentList results = query.getResults();

               ValueBuilder<StreetValue> streetValueBuilder = module.valueBuilderFactory().newValueBuilder( StreetValue.class );

               int count = 1;
               for(SolrDocument document : results )
               {
                  if( limit != -1 && limit < count )
                  {
                     break;
                  }
                  streetValueBuilder.prototype().address().set( (String)document.get( "address" )  );
                  streetValueBuilder.prototype().area().set( (String)document.get( "area" )  );

                  streetListBuilder.prototype().streets().get().add( streetValueBuilder.newInstance() );
                  count++;
               }
            }

            return streetListBuilder.newInstance();
         } catch( Throwable e)
         {
            log.error( "Could not get address list", e );

            // Return empty list
            return module.valueBuilderFactory().newValue(StreetList.class);
         }
      }

      public void reindex()
      {

         // make sure plugin is enabled before running reindex!
         if (config.configuration().enabled().get())
         {
            List<SolrInputDocument> added = new ArrayList<SolrInputDocument>();

            try
            {

               StreetValue streetValue = module.valueBuilderFactory().newValueBuilder( StreetValue.class ).prototype();
               streetValue.address().set( "%" );

               StreetList streets = cqc.query( config.configuration().url().get(),
                     StreetList.class, streetValue.buildWith().newInstance() );

               for (StreetValue street : streets.streets().get())
               {
                  SolrInputDocument document = new SolrInputDocument();
                  document.addField( "address", street.address().get() );
                  document.addField( "area", street.area().get() );

                  added.add( document );

               }


               try
               {
                  // empty the index
                  solr.getSolrServer( "sf-streetcache" ).deleteByQuery( "*:*" );
                  solr.getSolrServer( "sf-streetcache" ).add( added );

               } finally
               {
                  solr.getSolrServer( "sf-streetcache" ).commit( false, false );
                  config.configuration().lastLoaded().set( System.currentTimeMillis() );
                  config.save();
               }
            } catch (Throwable e)
            {

               log.error( "Could not create/update solr index for street address lookup.", e );
            }
         }
      }
   }
}
