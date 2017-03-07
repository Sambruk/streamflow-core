package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 03.03.17.
 */
public class EntityExportHelper
{

   private static final String SEPARATOR = ":separator:";
   private static final String IDENTITY_TABLE_NAME = "identity_type";

   private List<PropertyType> existsProperties;
   private Iterable<AssociationType> existsAssociations;
   private Iterable<ManyAssociationType> existsManyAssociations;
   private Map<String, Object> subProps;
   private Connection connection;
   private JSONObject entity;
   private ArrayList<PropertyType> allProperties;
   private String className;

   public void help() throws Exception
   {

      connection.setAutoCommit( false );

      String isExistQuery = "SELECT identity FROM " +
              tableName() +
              " WHERE identity = ?";

      final PreparedStatement isExistPS = connection
              .prepareStatement( isExistQuery );
      isExistPS.setString( 1, entity.getString( "identity" ) );
      final ResultSet isExistRS = isExistPS.executeQuery();

      if ( isExistRS.next() )
      {
         deleteEntityWithRelation( isExistRS.getString( "identity" ) );
      }

      isExistRS.close();


      String mainInsert = "INSERT INTO " +
              tableName() +
              " " +
              argumentsForEntityInsert( true ) +
              " VALUES " +
              argumentsForEntityInsert( false );

      final PreparedStatement statement = connection.prepareStatement( mainInsert );
      addArguments( statement );
      statement.executeUpdate();
      statement.close();

      saveAssociations();

      saveSubProperties();

      final String query = "INSERT INTO " +
              IDENTITY_TABLE_NAME +
              " (identity,type) VALUES (?,?)";

      final PreparedStatement ps = connection.prepareStatement( query );
      ps.setString( 1, entity.getString( "identity" ) );
      ps.setString( 2, className );
      ps.executeUpdate();
      ps.close();

      connection.commit();
      if ( connection != null && !connection.isClosed() )
      {
         connection.close();
      }

   }

   private String tableName()
   {
      return toSnackCaseFromCamelCase( className.substring( className.lastIndexOf( "." ) + 1 ) );
   }

   private void saveSubProperties() throws Exception
   {
      final Set<String> keys = subProps.keySet();
      for ( String key : keys )
      {

         final Object value = subProps.get( key );

         StringBuilder query = new StringBuilder( "UPDATE  " )
                 .append( tableName() )
                 .append( " SET " );
         StringBuilder temp = new StringBuilder();

         if ( value instanceof Collection || value instanceof Map )
         {

            if ( !( value instanceof Map ) )
            {
               final Object first = Iterables.first( ( Iterable<?> ) value );
               if ( first instanceof ValueComposite )
               {
                  for ( Object o : ( Collection<?> ) value )
                  {
                     if ( temp.length() == 0 )
                     {
                        temp.append( processValueComposite( ( ValueComposite ) o, key ) );
                     } else
                     {
                        temp.append( SEPARATOR ).append( processValueComposite( ( ValueComposite ) o, key ) );
                     }
                  }
               } else
               {
                  temp.append( processCollection( value, key ) );
               }
            } else
            {
               temp.append( processCollection( value, key ) );
            }

         } else if ( value instanceof ValueComposite )
         {
            temp.append( processValueComposite( ( ValueComposite ) value, key ) );
         }

         query
                 .append( toSnackCaseFromCamelCase( key ) )
                 .append( "= ?" )
                 .append( " WHERE identity = ?" );
         PreparedStatement preparedStatement = connection.prepareStatement( query.toString() );
         preparedStatement.setString( 1, temp.toString() );
         preparedStatement.setString( 2, entity.getString( "identity" ) );
         preparedStatement.executeUpdate();
         preparedStatement.close();

      }
   }

   private String processCollection( Object value, String key ) throws SQLException, JSONException
   {
      final String tableName = tableName() + "_" + toSnackCaseFromCamelCase( key );
      StringBuilder result = new StringBuilder();
      if ( value instanceof Collection )
      {
         for ( Object o : ( Collection<?> ) value )
         {

            String query = "INSERT INTO " +
                    tableName +
                    " (property_value) VALUES (?)";

            PreparedStatement preparedStatement = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
            preparedStatement.setString( 1, o.toString() );
            preparedStatement.executeUpdate();
            final ResultSet generatedKey = preparedStatement.getGeneratedKeys();
            generatedKey.next();
            int id = generatedKey.getInt( 1 );
            if ( result.length() > 0 )
            {
               result.append( SEPARATOR );
            }
            result.append( tableName )
                    .append( ";" )
                    .append( id );
            preparedStatement.close();
         }
      } else
      {
         final Map<?, ?> map = ( Map<?, ?> ) value;
         final Set<?> keySet = map.keySet();

         for ( Object o : keySet )
         {
            final String objKey = o.toString();
            final String objValue = map.get( o ).toString();

            String query = "INSERT INTO " +
                    tableName +
                    " (property_key,property_value) VALUES (?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
            preparedStatement.setString( 1, objKey );
            preparedStatement.setString( 2, objValue );
            preparedStatement.executeUpdate();
            final ResultSet generatedKey = preparedStatement.getGeneratedKeys();
            generatedKey.next();
            int id = generatedKey.getInt( 1 );
            if ( result.length() > 0 )
            {
               result.append( SEPARATOR );
            }
            result.append( tableName )
                    .append( ";" )
                    .append( id );
            preparedStatement.close();
         }

      }

      return result.toString();
   }

   private String processValueComposite( ValueComposite value, String name ) throws SQLException
   {
      final ValueExportHelper valueExportHelper = new ValueExportHelper();
      valueExportHelper.setName( name );
      valueExportHelper.setValue( value );
      valueExportHelper.setConnection( connection );
      return valueExportHelper.help();

   }


   private void saveAssociations() throws Exception
   {
      Map<String, String> associations = new LinkedHashMap<>();

      for ( AssociationType existsAssociation : existsAssociations )
      {
         final String name = existsAssociation.qualifiedName().name();
         final String identity = entity.getJSONObject( name ).getString( "identity" );
         associations.put( toSnackCaseFromCamelCase( name ), identity );
      }

      for ( ManyAssociationType existsManyAssociation : existsManyAssociations )
      {
         final String name = existsManyAssociation.qualifiedName().name();
         final JSONArray array = entity.getJSONArray( name );
         for ( int i = 0; ; i++ )
         {
            final JSONObject arrEl = array.optJSONObject( i );
            if ( arrEl == null )
            {
               break;
            }
            final String inMap = associations.get( name );
            associations.put( toSnackCaseFromCamelCase( name ), ( inMap == null ? "" : inMap + SEPARATOR ) + arrEl.getString( "identity" ) );
         }

      }

      if ( associations.size() > 0 )
      {
         final Set<String> keys = associations.keySet();

         StringBuilder query = new StringBuilder( "UPDATE  " )
                 .append( tableName() )
                 .append( " SET " );

         for ( String key : keys )
         {
            query
                    .append( key )
                    .append( " = ?," );
         }
         query
                 .deleteCharAt( query.length() - 1 )
                 .append( " WHERE identity = ?" );

         final PreparedStatement preparedStatement = connection.prepareStatement( query.toString() );

         int i = 1;
         for ( String key : keys )
         {
            preparedStatement.setString( i++, associations.get( key ) );
         }
         preparedStatement.setString( i, entity.getString( "identity" ) );
         preparedStatement.executeUpdate();
         preparedStatement.close();
      }


   }

   private void deleteEntityWithRelation( String identity ) throws SQLException
   {
      StringBuilder select = new StringBuilder( "SELECT " );

      boolean allow = false;
      int count = 0;
      for ( PropertyType property : allProperties )
      {
         if ( property.type().isValue() || property.type().type().name().equals( List.class.getName() ) || property.type().type().name().equals( Map.class.getName() ))
         {
            allow = true;
            select.append( toSnackCaseFromCamelCase( property.qualifiedName().name() ) )
                    .append( "," );
            count++;
         }

      }

      if ( allow )
      {
         select.deleteCharAt( select.length() - 1 ).append( " FROM " ).append( tableName() );

         final ResultSet resultSet = connection.prepareStatement( select.toString() ).executeQuery();

         for ( int i = 1; i <= count ; i++ )
         {
            resultSet.next();
            String id = resultSet.getString( i );

            if ( id == null )
            {
               continue;
            }

            String[] splitted;
            if ( id.contains( SEPARATOR ) )
            {
               splitted = id.split( SEPARATOR );
            } else
            {
               splitted = new String[]{id};
            }

            for ( String s : splitted )
            {
               final String[] split = s.split( ";" );
               final String delete = "DELETE FROM " + split[0] + " WHERE id = " + split[1];
               final PreparedStatement preparedStatement = connection.prepareStatement( delete );
               preparedStatement.executeUpdate();
               preparedStatement.close();
            }
         }
      }

      final String delete = "DELETE FROM " + tableName() + " WHERE identity = ?";
      final PreparedStatement preparedStatement = connection.prepareStatement( delete );
      preparedStatement.setString( 1, identity );
      preparedStatement.executeUpdate();
      preparedStatement.close();

      final String deleteFromIdentity = "DELETE FROM " + IDENTITY_TABLE_NAME + " WHERE identity = ?";
      final PreparedStatement ps = connection.prepareStatement( deleteFromIdentity );
      ps.setString( 1, identity );
      ps.executeUpdate();
      ps.close();
   }


   private void addArguments( PreparedStatement statement ) throws JSONException, SQLException, ClassNotFoundException
   {
      int i = 1;
      for ( PropertyType existsProperty : existsProperties )
      {
         final Class type = Class.forName( existsProperty.type().type().name() );

         final String name = existsProperty.qualifiedName().name();

         if ( subProps.get( name ) != null )
         {
            continue;
         }

         if ( Boolean.class.equals( type ) )
         {
            statement.setBoolean( i++, entity.getBoolean( name ) );
         } else if ( Integer.class.equals( type ) )
         {
            statement.setInt( i++, entity.getInt( name ) );
         } else if ( Long.class.equals( type ) )
         {
            statement.setLong( i++, entity.getLong( name ) );
         } else if ( Float.class.equals( type ) )
         {
            statement.setFloat( i++, ( float ) entity.getDouble( name ) );
         } else if ( Double.class.equals( type ) )
         {
            statement.setDouble( i++, entity.getDouble( name ) );
         } else if ( String.class.equals( type )
                 || type.isEnum()
                 || Date.class.equals( type )
                 || DateTime.class.equals( type ) )
         {
            statement.setString( i++, entity.getString( name ) );
         } else
         {
            throw new IllegalArgumentException();
         }


      }
   }

   private String argumentsForEntityInsert( boolean names )
   {
      StringBuilder temp = new StringBuilder( "(" );
      for ( PropertyType existsProperty : existsProperties )
      {
         final String name = existsProperty.qualifiedName().name();
         if ( subProps.get( name ) == null )
         {
            if ( names )
            {
               temp.append( toSnackCaseFromCamelCase( name ) )
                       .append( "," );
            } else
            {
               temp.append( "?," );
            }
         }
      }
      return temp
              .deleteCharAt( temp.length() - 1 )
              .append( ")" )
              .toString();
   }

   private String toSnackCaseFromCamelCase( String str )
   {
      StringBuilder stringBuilder = new StringBuilder();
      for ( int i = 0; i < str.length(); i++ )
      {
         char ch = str.charAt( i );
         if ( i == 0 )
         {
            ch = Character.toLowerCase( ch );
         }
         if ( Character.isUpperCase( ch ) )
         {
            stringBuilder.append( '_' );
            stringBuilder.append( Character.toLowerCase( ch ) );
         } else
         {
            stringBuilder.append( ch );
         }
      }
      return stringBuilder.toString();
   }

   //setters


   public void setExistsProperties( Iterable<PropertyType> existsProperties )
   {
      this.existsProperties = new LinkedList<>();
      for ( PropertyType existsProperty : existsProperties )
      {
         this.existsProperties.add( existsProperty );
      }
   }

   public void setExistsAssociations( Iterable<AssociationType> existsAssociations )
   {
      this.existsAssociations = existsAssociations;
   }

   public void setExistsManyAssociations( Iterable<ManyAssociationType> existsManyAssociations )
   {
      this.existsManyAssociations = existsManyAssociations;
   }

   public void setSubProps( Map<String, Object> subProps )
   {
      this.subProps = subProps;
   }

   public void setConnection( Connection connection )
   {
      this.connection = connection;
   }

   public void setEntity( JSONObject entity )
   {
      this.entity = entity;
   }

   public void setAllProperties( Set<PropertyType> allProperties )
   {
      this.allProperties = new ArrayList<>( allProperties );
   }

   public void setClassName( String className )
   {
      this.className = className;
   }
}
