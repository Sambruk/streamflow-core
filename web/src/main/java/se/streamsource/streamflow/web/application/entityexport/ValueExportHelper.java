package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.runtime.types.MapType;
import org.qi4j.runtime.types.ValueCompositeType;
import org.qi4j.runtime.value.ValuePropertyModel;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.value.ValueDescriptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ruslan on 06.03.17.
 */
public class ValueExportHelper extends AbstractExportHelper
{

   public static final String COLUMN_DESCRIPTION_SUFFIX = "_table";
   private ValueComposite value;

   public SingletonMap help() throws Exception
   {

      ValueDescriptor valueDescriptor = module.valueDescriptor( name() );
      JSONObject jsonObject = new JSONObject( value.toJSON() );

      final StateDescriptor state = valueDescriptor.state();
      final List<PropertyDescriptor> props = new ArrayList<>( state.properties() );

      final Map<Integer, String> propertiesKeys = new LinkedHashMap<>();

      StringBuilder query = new StringBuilder( "INSTERT INTO " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " (" );

      int count = 0;

      for( PropertyDescriptor property : props )
      {
         final String name = property.qualifiedName().name();

         final Object object = jsonObject.opt( name );
         if( object != null
                 && !object.equals( JSONObject.NULL )
                 && !jsonEmpty( object.toString() ) )
         {
            final ValueType type = ( ( ValuePropertyModel ) property ).propertyType().type();

            if( type instanceof ValueCompositeType )
            {
               count++;
               propertiesKeys.put( count, name );
               query.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                       .append( "," );
            } else if( type instanceof CollectionType )
            {
               final CollectionType collectionType = ( CollectionType ) type;

               final ValueType valueType = collectionType.collectedType();
               if( valueType.isValue() )
               {
                  count++;
                  propertiesKeys.put( count, name );
                  query.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                          .append( "," );
               } else
               {
                  count++;
                  propertiesKeys.put( count, name );
                  query.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                          .append( "," );
               }

            } else if( type instanceof MapType )
            {
               count++;
               propertiesKeys.put( count, name );
               query.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                       .append( "," );
            } else
            {
               count++;
               propertiesKeys.put( count, name );
               query.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                       .append( "," );
            }
         }

      }

      query
              .deleteCharAt( query.length() - 1 )
              .append( ") VALUES (" );

      for( int i = 1; i < count; i++ )
      {
         query.append( "?," );
      }

      query
              .deleteCharAt( query.length() - 1 )
              .append( ")" );

      final PreparedStatement preparedStatement = connection.prepareStatement( query.toString(), Statement.RETURN_GENERATED_KEYS );

      for( int i = 1; i <= count; i++ )
      {
         final String key = propertiesKeys.get( i );
         final PropertyDescriptor property = state.getPropertyByName( key );
         final ValueType type = ( ( ValuePropertyModel ) property ).propertyType().type();
         final Class<?> clazz = Class.forName( type.type().name() );

         if( type.isValue() )
         {
            final ValueComposite value = ( ValueComposite ) type.fromJSON( jsonObject.getJSONObject( key ), module );
//            preparedStatement.setString( i, processValueComposite( value ) );
         } else if( type instanceof CollectionType || type instanceof MapType )
         {
            if( type instanceof CollectionType && ( ( CollectionType ) type ).collectedType().isValue() )
            {
               final JSONArray jsonArray = jsonObject.getJSONArray( key );
               StringBuilder result = new StringBuilder();
               for( int j = 0; ; j++ )
               {
                  final Object obj = jsonArray.optJSONObject( j );
                  if( obj == null )
                  {
                     break;
                  }

                  final ValueComposite value = ( ValueComposite ) type.fromJSON( obj, module );
                  result.append( processValueComposite( value ) );
               }
               preparedStatement.setString( i, result.toString() );
            } else
            {
               final ValueComposite value = ( ValueComposite ) type.fromJSON( jsonObject.getJSONObject( key ), module );
               preparedStatement.setString( i, processCollection( value, key ) );
            }
         } else
         {
            setSimpleType(preparedStatement, clazz, jsonObject, key, i);
         }
      }

      preparedStatement.executeUpdate();

      final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      final int id = generatedKeys.next() ? generatedKeys.getInt( 0 ) : 0;
      preparedStatement.close();
      return new SingletonMap();
   }

   private boolean jsonEmpty( String json )
   {
      return json.isEmpty() || json.equals( "{}" ) || json.equals( "[]" );
   }

   @Override
   protected String tableName()
   {
      return toSnackCaseFromCamelCase( classSimpleName( name() ) );
   }

   private String name()
   {
      return value.type().getName();
   }

   // setters

   public void setValue( ValueComposite value )
   {
      this.value = value;
   }


}
