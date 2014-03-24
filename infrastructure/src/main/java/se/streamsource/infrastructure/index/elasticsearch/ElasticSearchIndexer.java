/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.infrastructure.index.elasticsearch;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.util.Primitives;

import java.lang.reflect.*;
import java.util.*;

/**
 * Back ported from Qi4j 2.0
 * courtesy of Paul Merlin
 *
 * Listen to Entity state changes and index them in ElasticSearch.
 *
 * QUID Use two indices, one for strict queries, one for full text and fuzzy search?
 */
@Mixins( ElasticSearchIndexer.Mixin.class )
public interface ElasticSearchIndexer
        extends StateChangeListener
{

    class Mixin
            implements StateChangeListener
    {

        private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchIndexer.class);
        @Structure
        private ModuleSPI module;
        @Service
        private EntityStore entityStore;

        @This
        private ElasticSearchSupport support;

        public void emptyIndex()
        {
            support.client().admin().indices().prepareDelete( support.index() ).execute().actionGet();
        }

        public void notifyChanges( Iterable<EntityState> changedStates )
        {
            // All updated or new states
            Map<String, EntityState> newStates = new HashMap<String, EntityState>();
            for( EntityState eState : changedStates )
            {
                if( eState.status() == EntityStatus.UPDATED || eState.status() == EntityStatus.NEW )
                {
                    newStates.put( eState.identity().identity(), eState );
                }
            }

            EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase("Load associations for indexing"),
                    module );

            // Bulk index request builder
            BulkRequestBuilder bulkBuilder = support.client().prepareBulk();

            // Handle changed entity states
            for( EntityState changedState : changedStates )
            {
                if( changedState.entityDescriptor().entityType().queryable() )
                {
                    switch( changedState.status() )
                    {
                        case REMOVED:
                            LOGGER.trace( "Removing Entity State from Index: {}", changedState );
                            remove( bulkBuilder, changedState.identity().identity() );
                            break;
                        case UPDATED:
                            LOGGER.trace( "Updating Entity State in Index: {}", changedState );
                            remove( bulkBuilder, changedState.identity().identity() );
                            String updatedJson = toJSON( changedState, newStates, uow );
                            LOGGER.trace( "Will index: {}", updatedJson );
                            index( bulkBuilder, changedState.identity().identity(), updatedJson );
                            break;
                        case NEW:
                            LOGGER.trace( "Creating Entity State in Index: {}", changedState );
                            String newJson = toJSON( changedState, newStates, uow );
                            LOGGER.trace( "Will index: {}", newJson );
                            index( bulkBuilder, changedState.identity().identity(), newJson );
                            break;
                        case LOADED:
                        default:
                            // Ignored
                            break;
                    }
                }
            }

            uow.discard();

            if( bulkBuilder.numberOfActions() > 0 )
            {

                // Execute bulk actions
                BulkResponse bulkResponse = bulkBuilder.execute().actionGet();

                // Handle errors
                if( bulkResponse.hasFailures() )
                {
                    throw new ElasticSearchIndexException( bulkResponse.buildFailureMessage() );
                }

                LOGGER.debug( "Indexing changed Entity states took {}ms", bulkResponse.getTookInMillis() );

                // Refresh index
                support.client().admin().indices().prepareRefresh( support.index() ).execute().actionGet();

            }
        }

        private void remove( BulkRequestBuilder bulkBuilder, String identity )
        {
            bulkBuilder.add( support.client().
                    prepareDelete( support.index(), support.entitiesType(), identity ) );
        }

        private void index( BulkRequestBuilder bulkBuilder, String identity, String json )
        {
            bulkBuilder.add( support.client().
                    prepareIndex( support.index(), support.entitiesType(), identity ).
                    setSource( json ) );
        }

        /**
         * <pre>
         * {
         *  "_identity": "ENTITY-IDENTITY",
         *  "_types": [ "All", "Entity", "types" ],
         *  "property.name": property.value,
         *  "association.name": "ASSOCIATED-IDENTITY",
         *  "manyassociation.name": [ "ASSOCIATED", "IDENTITIES" ]
         * }
         * </pre>
         */
        private String toJSON( EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
        {
            try
            {
                JSONObject json = new JSONObject();

                json.put( "_identity", state.identity().identity() );

                json.put( "_types", Iterables.addAll( new ArrayList<String>(), Iterables.map( toClassName(), state.entityDescriptor().mixinTypes()) ) );
                EntityType entityType = state.entityDescriptor().entityType();
                EntityDescriptor entityDesc = state.entityDescriptor();
                // Properties
                for( PropertyType propType : entityType.properties() )
                {
                    if( propType.queryable() )
                    {
                        String key = propType.qualifiedName().name();
                        Object value = state.getProperty(propType.qualifiedName());
                        if( value == null || Primitives.isPrimitiveValue(value) )
                        {
                            json.put( key, value );
                        }
                        else
                        {
                            // TODO Theses tests are pretty fragile, find a better way to fix this, Jackson API should behave better
                            String serialized = propType.type().toJSON(value).toString();
                            if( serialized.startsWith( "{" ) )
                            {
                                json.put( key, new JSONObject( serialized ) );
                            }
                            else if( serialized.startsWith( "[" ) )
                            {
                                json.put( key, new JSONArray( serialized ) );
                            }
                            else
                            {
                                json.put( key, serialized );
                            }
                        }
                    }
                }

                // Associations
                for( AssociationDescriptor assocDesc : entityDesc.state().associations() )
                {
                    if( assocDesc.associationType().queryable() )
                    {
                        String key = assocDesc.qualifiedName().name();
                        EntityReference associated = state.getAssociation(assocDesc.qualifiedName());
                        Object value;
                        if( associated == null )
                        {
                            value = null;
                        }
                        else
                        {
                            if( assocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                if( newStates.containsKey( associated.identity() ) )
                                {
                                    value = new JSONObject( toJSON( newStates.get( associated.identity() ), newStates, uow ) );
                                }
                                else
                                {
                                    EntityState assocState = uow.getEntityState( EntityReference.parseEntityReference( associated.identity() ) );
                                    value = new JSONObject( toJSON( assocState, newStates, uow ) );
                                }
                            }
                            else
                            {
                                value = new JSONObject( Collections.singletonMap("identity", associated.identity()) );
                            }
                        }
                        json.put( key, value );
                    }
                }

                // ManyAssociations
                for( ManyAssociationDescriptor manyAssocDesc : entityDesc.state().manyAssociations() )
                {
                    if( manyAssocDesc.manyAssociationType().queryable() )
                    {
                        String key = manyAssocDesc.qualifiedName().name();
                        JSONArray array = new JSONArray();
                        ManyAssociationState associateds = state.getManyAssociation(manyAssocDesc.qualifiedName());
                        for( EntityReference associated : associateds )
                        {
                            if( manyAssocDesc.isAggregated() || support.indexNonAggregatedAssociations() )
                            {
                                if( newStates.containsKey( associated.identity() ) )
                                {
                                    array.put( new JSONObject( toJSON( newStates.get( associated.identity() ), newStates, uow ) ) );
                                }
                                else
                                {
                                    EntityState assocState = uow.getEntityState(EntityReference.parseEntityReference(associated.identity()));
                                    array.put( new JSONObject( toJSON( assocState, newStates, uow ) ) );
                                }
                            }
                            else
                            {
                                array.put( new JSONObject( Collections.singletonMap( "identity", associated.identity() ) ) );
                            }
                        }
                        json.put( key, array );
                    }
                }

                return json.toString();
            }
            catch( JSONException e )
            {
                throw new ElasticSearchIndexException( "Could not index EntityState", e );
            }
        }

        private Function<Type, String> toClassName()
        {
            return new Function<Type, String>()
            {
                public String map( Type type )
                {
                    return RAW_CLASS.map( type ).getName();
                }
            };
        }

        /**
         * Function that extract the raw class of a type.
         */
        private final Function<Type, Class<?>> RAW_CLASS = new Function<Type, Class<?>>()
        {
            public Class<?> map( Type genericType )
            {
                // Calculate raw type
                if( genericType instanceof Class )
                {
                    return (Class<?>) genericType;
                }
                else if( genericType instanceof ParameterizedType)
                {
                    return (Class<?>) ( (ParameterizedType) genericType ).getRawType();
                }
                else if( genericType instanceof TypeVariable)
                {
                    return (Class<?>) ( (TypeVariable) genericType ).getGenericDeclaration();
                }
                else if( genericType instanceof WildcardType)
                {
                    return (Class<?>) ( (WildcardType) genericType ).getUpperBounds()[ 0 ];
                }
                else if( genericType instanceof GenericArrayType)
                {
                    Object temp = Array.newInstance( (Class<?>) ( (GenericArrayType) genericType ).getGenericComponentType(), 0 );
                    return temp.getClass();
                }
                throw new IllegalArgumentException( "Could not extract the raw class of " + genericType );
            }
        };
    }

}
