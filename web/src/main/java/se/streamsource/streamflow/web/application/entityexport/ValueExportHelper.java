package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.property.PropertyDescriptor;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 06.03.17.
 */
public class ValueExportHelper extends AbstractExportHelper
{

   private ValueComposite value;

   public SingletonMap help() throws Exception
   {

      List<PropertyDescriptor> properties = new ArrayList<>( module.valueDescriptor( value.type().getName() ).state().properties() );

      createSubPropertyTableIfNotExists( tableName() );

      final String query = mainInsert( properties );

      PreparedStatement preparedStatement = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );

      final Map<QualifiedName, Boolean> collections = addArguments( properties, preparedStatement );

      preparedStatement.executeUpdate();

      final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      generatedKeys.next();
      final int id = generatedKeys.getInt( 1 );

      saveSubProperties( collections, id );

      return new SingletonMap(id, tableName());
   }

   private void saveSubProperties( Map<QualifiedName, Boolean> collections, int id ) throws Exception
   {

      final Set<QualifiedName> qualifiedNames = collections.keySet();

      for ( QualifiedName qualifiedName : qualifiedNames )
      {
         final Boolean isValue = collections.get( qualifiedName );

         if ( isValue )
         {
            List<SingletonMap> collectionOfValues = new ArrayList<>();
            for ( Object o : ( Collection<?> ) value.state().getProperty( qualifiedName ).get() )
            {
               collectionOfValues.add( processValueComposite( ( ValueComposite ) o ) );
            }

            final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( qualifiedName.name() ) + "_cross_ref";

            final String associationTable = ( String ) Iterables.first( collectionOfValues ).getValue();

            createCrossRefTableIfNotExists( tableName, associationTable, detectSqlType( Integer.class ), detectSqlType( Integer.class ) );

            final String insertSubProperties = "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                    " (owner_id,link_id) VALUES (?,?)";

            try ( final PreparedStatement preparedStatement = connection.prepareStatement( insertSubProperties ) )
            {

               for ( SingletonMap val : collectionOfValues )
               {
                  preparedStatement.setInt( 1, id );
                  preparedStatement.setInt( 2, ( Integer ) val.getKey() );
                  preparedStatement.addBatch();
               }

               preparedStatement.executeBatch();
            }
         } else
         {
            processCollection( qualifiedName.name(),
                    value.state().getProperty( qualifiedName ).get(),
                    new PreparedStatementIntegerBinder( id, detectSqlType( Integer.class ) )
            );
         }
      }

   }

   private Map<QualifiedName, Boolean> addArguments( List<PropertyDescriptor> properties, PreparedStatement preparedStatement ) throws Exception
   {
      Map<QualifiedName, Boolean> existsCollections = new LinkedHashMap<>();

      try ( final Statement statement = connection.createStatement() )
      {

         Set<String> triggerStatements = new LinkedHashSet<>();

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
                     final ValueComposite valueComposite = ( ValueComposite ) val;

                     final SingletonMap singletonMap = processValueComposite( valueComposite );

                     final String triggerStatement = addColumn( toSnackCaseFromCamelCase( property.qualifiedName().name() ), detectType( valueComposite ), statement );

                     if ( !triggerStatement.isEmpty() )
                     {
                        triggerStatements.add( triggerStatement );
                     }

                     preparedStatement.setInt( i++, ( Integer ) singletonMap.getKey() );

                  } else
                  {
                     setSimpleType( preparedStatement, val, i++ );
                  }
               }

            } else if ( type instanceof ParameterizedType )
            {
               final ParameterizedType parameterizedType = ( ParameterizedType ) type;
               final Type ownerType = parameterizedType.getRawType();
               if ( ownerType.equals( Map.class ) )
               {
                  final Map val = ( Map ) value.state().getProperty( property.qualifiedName() ).get();
                  final boolean isValue = false;
                  if ( !isEmptyOrNull( val ) )
                  {
                     existsCollections.put( property.qualifiedName(), isValue );
                  }
               } else
               {
                  final Class<?> actualType = ( Class<?> ) parameterizedType.getActualTypeArguments()[0];
                  final boolean isValue = ValueComposite.class.isAssignableFrom( actualType )
                          && EntityInfo.from( actualType ) == EntityInfo.UNKNOWN;
                  final Collection val = ( Collection ) value.state().getProperty( property.qualifiedName() ).get();
                  if ( !isEmptyOrNull( val ) )
                  {
                     existsCollections.put( property.qualifiedName(), isValue );
                  }
               }
            } else
            {
               new IllegalArgumentException( String.format( "Unknown parameter of type %s", type.getClass().getName() ) );
            }

         }

         statement.executeBatch();

         createTrigger( triggerStatements );
      }


      return existsCollections;

   }

   private String mainInsert( List<PropertyDescriptor> properties ) throws Exception
   {

      StringBuilder query = new StringBuilder( "INSERT INTO  " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " (" );

      Set<String> triggerStatements = new LinkedHashSet<>();
      int i = 0;
      try ( final Statement statement = connection.createStatement() )
      {
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

                  final String name = toSnackCaseFromCamelCase( property.qualifiedName().name() );


                  final String triggerStatement = addColumn( name, (Class<?>) property.type(), statement );

                  if ( !triggerStatement.isEmpty() )
                  {
                     triggerStatements.add( triggerStatement );
                  }

                  query
                          .append( escapeSqlColumnOrTable( name ) )
                          .append( "," );
                  i++;
               }
            }
         }

         statement.executeBatch();

      }

      createTrigger( triggerStatements );

      if ( i == 0 )
      {
         return "INSERT INTO " + escapeSqlColumnOrTable( tableName() ) + " DEFAULT VALUES";
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
      //exclusions
      if ( FieldValue.class.isAssignableFrom( value.type() ) )
      {
         return toSnackCaseFromCamelCase( FieldValue.class.getSimpleName() );
      } else if ( ActionValue.class.isAssignableFrom( value.type() ) )
      {
         return toSnackCaseFromCamelCase( ActionValue.class.getSimpleName() );
      }

      return toSnackCaseFromCamelCase( value.type().getSimpleName() );
   }

   // setters

   public void setValue( ValueComposite value )
   {
      this.value = value;
   }


}
