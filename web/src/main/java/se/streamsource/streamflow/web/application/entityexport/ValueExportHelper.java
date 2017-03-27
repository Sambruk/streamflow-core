package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 06.03.17.
 */
public class ValueExportHelper extends AbstractExportHelper
{

   public static final String COLUMN_DESCRIPTION_SUFFIX = "_table";
   private ValueComposite value;

   public SingletonMap help() throws Exception
   {

      List<PropertyDescriptor> properties = new ArrayList<>( module.valueDescriptor( value.type().getName() ).state().properties() );

      final String query = mainInsert( properties );

      PreparedStatement preparedStatement = connection.prepareStatement( query );

      addArguments( properties, preparedStatement );

      return new SingletonMap();
   }

   private void addArguments( List<PropertyDescriptor> properties, PreparedStatement statement ) throws Exception
   {

      int i = 1;
      for ( PropertyDescriptor property : properties )
      {
         final Type type = property.type();
         if ( type instanceof Class )
         {

            final Object val = value.state().getProperty( property.qualifiedName() ).get();
            if ( val != null )
            {
               if ( val instanceof String && ( ( String ) val ).isEmpty() )
               {
                  continue;
               }

               if ( ValueComposite.class.isAssignableFrom( val.getClass() )
                       && EntityInfo.from( val.getClass() ) == EntityInfo.UNKNOWN )
               {

                  final SingletonMap singletonMap = processValueComposite( ( ValueComposite ) val );
                  statement.setInt( i++, ( Integer ) singletonMap.getKey() );

               } else
               {
                  setSimpleType( statement, val, i++ );
               }

            }

         } else if ( type instanceof ParameterizedType )
         {
            final ParameterizedType parameterizedType = ( ParameterizedType ) type;
            final Type ownerType = parameterizedType.getOwnerType();
            if ( ownerType.equals( Map.class ) )
            {
               final Map val = ( Map ) value.state().getProperty( property.qualifiedName() ).get();
               if ( !isEmptyOrNull( val ) )
               {
                  processCollection( key, value, table, new PreparedStatementIntegerBinder( id, detectSqlType( Integer.class ) ) );

               }
            } else
            {
               final Class<?> actualType = ( Class<?> ) parameterizedType.getActualTypeArguments()[0];
               final boolean isValue = ValueComposite.class.isAssignableFrom( actualType )
                       && EntityInfo.from( actualType ) == EntityInfo.UNKNOWN;

               final Collection val = ( Collection ) value.state().getProperty( property.qualifiedName() ).get();
               if ( !isEmptyOrNull( val ) )
               {
                  processCollection( key, value, table, new PreparedStatementIntegerBinder( id, detectSqlType( Integer.class ) ) );
               }
            }
         } else
         {
            new IllegalArgumentException( String.format( "Unknown parameter of type %s", type.getClass().getName() ) );
         }

      }

   }

   private String mainInsert( List<PropertyDescriptor> properties ) throws Exception
   {

      StringBuilder query = new StringBuilder( "INSERT INTO  " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " (" );

      int i = 0;
      for ( PropertyDescriptor property : properties )
      {
         if ( property.type() instanceof Class )
         {
            final Object val = value.state().getProperty( property.qualifiedName() ).get();
            if ( val != null )
            {
               if ( val instanceof String && ( ( String ) val ).isEmpty() )
               {
                  continue;
               }

               query
                       .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( property.qualifiedName().name() ) ) )
                       .append( "," );
               i++;
            }
         }
      }

      query.deleteCharAt( query.length() - 1 )
              .append( ") VALUES (" );

      for ( int j = 0; j < i; j++ )
      {
         query.append( "?," );
      }

      return query
              .deleteCharAt( query.length() - 1 )
              .append( ")" )
              .toString();

   }

   private boolean isEmptyOrNull( Map map )
   {
      return map == null || map.isEmpty();
   }

   private boolean isEmptyOrNull( Collection collection )
   {
      return collection == null || collection.isEmpty();
   }

   @Override
   protected String tableName()
   {
      return toSnackCaseFromCamelCase( value.type().getSimpleName() );
   }

   // setters

   public void setValue( ValueComposite value )
   {
      this.value = value;
   }


}
