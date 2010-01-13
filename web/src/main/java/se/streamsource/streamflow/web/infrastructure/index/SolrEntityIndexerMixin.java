/*
 * Copyright (c) 2009, Rickard �berg. All Rights Reserved.
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
package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.SchemaField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.StateChangeListener;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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

   public void activate() throws Exception
   {
      server = solr.getSolrServer();
      indexedFields = solr.getSolrCore().getSchema().getFields();
   }

   public void passivate() throws Exception
   {
   }

   public void notifyChanges( Iterable<EntityState> entityStates )
   {
      try
      {
         try
         {
            // Figure out what to update
            for (EntityState entityState : entityStates)
            {
               if (entityState.status().equals( EntityStatus.REMOVED ))
               {
                  removeEntityState( entityState.identity(), server );
               } else if (entityState.status().equals( EntityStatus.UPDATED ))
               {
                  removeEntityState( entityState.identity(), server );
                  indexEntityState( entityState, server );
               } else if (entityState.status().equals( EntityStatus.NEW ))
               {
                  indexEntityState( entityState, server );
               }
            }
         }
         finally
         {
            if (server != null)
            {
               server.commit();
            }
         }
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         //TODO What shall we do with the exception?
      }
   }

   private void indexEntityState( final EntityState entityState,
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
      server.add( input );
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

   private void removeEntityState( final EntityReference identity,
                                   final SolrServer server )
         throws IOException, SolrServerException
   {
      server.deleteById( identity.identity() );
   }
}