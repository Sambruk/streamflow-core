package se.streamsource.streamflow.web.domain.util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.property.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Converts an entity to a json string aggregating all associations and many associations too.
 */
public class ToJson {

    private static Logger logger = LoggerFactory.getLogger( ToJson.class.getName() );
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
    public static String toJSON( EntityState state, Map<String, EntityState> newStates, EntityStoreUnitOfWork uow )
    {
        long start = System.nanoTime();
        JSONObject json = null;
        try
        {
            json = new JSONObject();

            json.put( "_identity", state.identity().identity() );

            json.put( "_types", state.entityDescriptor().entityType().toString() );
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
                    json.put( key, array );
                }
            }

            return json.toString();
        }
        catch( JSONException e )
        {
            logger.info( "Faild to convert Entity to Json: " + state.identity().identity(), e);
            throw new RuntimeException("Faild to convert Entity to Json: " + state.identity().identity(), e);
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
