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
import org.apache.solr.common.*;
import org.apache.solr.core.*;
import org.apache.solr.schema.*;
import org.json.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.service.*;
import org.qi4j.library.rdf.entity.*;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entitystore.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

/**
 * JAVADOC Add JavaDoc
 */
public class SolrEntityIndexerMixin
      implements StateChangeListener, Activatable
{
   @Service
   private EmbeddedSolrService solr;

   @Uses
   private EntityStateSerializer stateSerializer;

   private ValueFactory valueFactory = new ValueFactoryImpl();

   private SolrServer server;
   private Map<String, SchemaField> indexedFields;

   Logger logger = LoggerFactory.getLogger( getClass() );

   public void activate() throws Exception
   {
      server = solr.getSolrServer();
      SolrCore solrCore = solr.getSolrCore();
      try
      {
         indexedFields = solrCore.getSchema().getFields();
      } finally
      {
         solrCore.close();
      }
   }

   public void passivate() throws Exception
   {
      server = null;
      indexedFields = null;
   }

   public void notifyChanges( Iterable<EntityState> entityStates )
   {
      try
      {
         try
         {
            // Figure out what to update
            List<String> deleted = null;
            List<SolrInputDocument> added = new ArrayList<SolrInputDocument>();
            for (EntityState entityState : entityStates)
            {
               if (entityState.status().equals( EntityStatus.REMOVED ))
               {
                  if (deleted == null)
                     deleted = new ArrayList<String>();
                  deleted.add( entityState.identity().identity() );
               } else if (entityState.status().equals( EntityStatus.UPDATED ))
               {
                  added.add( indexEntityState( entityState, server ) );
               } else if (entityState.status().equals( EntityStatus.NEW ))
               {
                  added.add( indexEntityState( entityState, server ) );
               }
            }

            // Send changes to Solr
            if (deleted != null)
               server.deleteById( deleted );
            if (!added.isEmpty())
               server.add( added );
         }
         finally
         {
            if (server != null)
            {
               server.commit( false, false );
            }
         }
      }
      catch (Throwable e)
      {
         logger.error( "Could not update Solr", e );
         //TODO What shall we do with the exception?
      }
   }

   private SolrInputDocument indexEntityState( final EntityState entityState,
                                               final SolrServer server )
         throws IOException, SolrServerException, JSONException
   {
      Graph graph = new GraphImpl();
      stateSerializer.serialize( entityState, false, graph );

      SolrInputDocument input = new SolrInputDocument();
      input.addField( "id", entityState.identity().identity() );
      input.addField( "type", entityState.entityDescriptor().entityType().type().name() );
      input.addField( "lastModified", new Date( entityState.lastModified() ) );

      for (Statement statement : graph)
      {
         SchemaField field = indexedFields.get( statement.getPredicate().getLocalName() );
         if (field != null)
         {
            if (statement.getObject() instanceof Literal)
            {
               String value = statement.getObject().stringValue();
               if (field.getType().getTypeName().equals( "json" ))
               {
                  if (value.charAt( 0 ) == '[')
                  {
                     JSONArray array = new JSONArray( value );
                     indexJson( input, array );
                  } else if (value.charAt( 0 ) == '{')
                  {
                     JSONObject object = new JSONObject( value );
                     indexJson( input, object );
                  }
               } else
               {
                  input.addField( field.getName(), value );
               }
            } else if (statement.getObject() instanceof URI && !"type".equals( field.getName() ))
            {
               String value = statement.getObject().stringValue();
               value = value.substring( value.lastIndexOf( ':' ) + 1, value.length() );
               String name = field.getName();
               input.addField( name, value );
            } else if (statement.getObject() instanceof BNode)
            {
               Iterator<Statement> seq = graph.match( (Resource) statement.getObject(), new URIImpl( "http://www.w3.org/1999/02/22-rdf-syntax-ns#li" ), null, (Resource) null );
               while (seq.hasNext())
               {
                  Statement seqStatement = seq.next();
                  String value = seqStatement.getObject().stringValue();
                  value = value.substring( value.lastIndexOf( ':' ) + 1, value.length() );

                  input.addField( field.getName(), value );
               }
            }
         }

      }

      return input;
   }

   private void indexJson( SolrInputDocument input, Object object ) throws JSONException
   {
      if (object instanceof JSONArray)
      {
         JSONArray array = (JSONArray) object;
         for (int i = 0; i < array.length(); i++)
            indexJson( input, array.get( i ) );
      } else
      {
         JSONObject jsonObject = (JSONObject) object;
         Iterator keys = jsonObject.keys();
         while (keys.hasNext())
         {
            Object name = keys.next();
            Object value = jsonObject.get( name.toString() );
            if (value instanceof JSONObject || value instanceof JSONArray)
            {
               indexJson( input, value );
            } else
            {
               SchemaField field = indexedFields.get( name.toString() );
               if (field != null)
               {
                  input.addField( name.toString(), jsonObject.get( name.toString() ) );
               }
            }
         }
      }
   }
}