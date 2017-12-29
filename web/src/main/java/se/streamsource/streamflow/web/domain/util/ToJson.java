/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Function;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.util.Primitives;

import java.lang.reflect.*;
import java.util.Collections;

/**
 * Converts an entity to a json string aggregating all associations and many associations too.
 */
public class ToJson {

    private static Logger logger = LoggerFactory.getLogger( ToJson.class.getName() );

    ModuleSPI module;

    EntityStore entityStore;

    EntityStoreUnitOfWork uow;

    public ToJson(@Structure ModuleSPI module, @Service EntityStore entityStore )
    {
        this.module = module;
        this.entityStore = entityStore;
        this.uow = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase("toJson"), module );
    }

    public  String toJSON( EntityState state )
    {
        return toJSON(state, false);
    }

    public  String toJSON( EntityState state, boolean full )
    {
        return toJSON(state, full, true);
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
    private String toJSON(EntityState state, boolean ignoreQueryable, boolean aggregateAssociations)
    {
        JSONObject json;
        try
        {
            json = new JSONObject();

            json.put("_identity", state.identity());

            EntityDescriptor entityDesc = state.entityDescriptor();
            EntityType entityType = entityDesc.entityType();

            json.put("_type", entityType.toString());

            // Properties
            for( PropertyType propType : entityType.properties() )
            {
                if( ignoreQueryable || propType.queryable() )
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
                if( ignoreQueryable || assocDesc.associationType().queryable() )
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
                        if( aggregateAssociations )
                        {

                            try
                            {
                                EntityState assocState = uow.getEntityState( EntityReference.parseEntityReference( associated.identity() ) );
                                value = new JSONObject( toJSON( assocState, ignoreQueryable, false) );
                            } catch ( EntityNotFoundException e )
                            {
                                value = new JSONObject( Collections.singletonMap("identity", associated.identity() + " aggregation impossible") );
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
                if( ignoreQueryable || manyAssocDesc.manyAssociationType().queryable() )
                {
                    String key = manyAssocDesc.qualifiedName().name();
                    JSONArray array = new JSONArray();
                    ManyAssociationState associateds = state.getManyAssociation(manyAssocDesc.qualifiedName());
                    for( EntityReference associated : associateds )
                    {
                        if( aggregateAssociations  )
                        {
                            try
                            {
                                EntityState assocState = uow.getEntityState(EntityReference.parseEntityReference(associated.identity()));
                                array.put( new JSONObject( toJSON( assocState, ignoreQueryable, false ) ) );
                            } catch ( EntityNotFoundException e )
                            {
                                array.put( new JSONObject( Collections.singletonMap("identity", associated.identity() + " aggregation impossible") ) );
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
            logger.info("Faild to convert Entity to Json: " + state.identity(), e);
            throw new RuntimeException("Faild to convert Entity to Json: " + state.identity(), e);
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
