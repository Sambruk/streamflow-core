package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
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
   private static final String ESCAPE_SQL = "`";

   private List<PropertyType> existsProperties;
   private Iterable<AssociationType> existsAssociations;
   private Iterable<ManyAssociationType> existsManyAssociations;
   private Map<String, Object> subProps;
   private Connection connection;
   private JSONObject entity;
   private ArrayList<PropertyType> allProperties;
   private ArrayList<ManyAssociationType> allManyAssociations;
   private ArrayList<AssociationType> allAssociations;
   private String className;

   public void help() throws Exception
   {

      connection.setAutoCommit( false );

      final String identity = entity.getString( "identity" );
      final ResultSet isExistRS = selectFromWhereId( tableName(), identity );

      checkEntityExists( className, identity );

      if ( isExistRS.next() )
      {
         deleteEntityAndRelations( isExistRS.getString( "identity" ) );
      }

      isExistRS.close();

      mainUpdate( identity );

      saveAssociations();

      saveManyAssociations();

      saveSubProperties();

      connection.commit();
      if ( connection != null && !connection.isClosed() )
      {
         connection.close();
      }

   }

   private void saveManyAssociations() throws Exception
   {
      for ( ManyAssociationType existsManyAssociation : existsManyAssociations )
      {
         String tableName = tableName() + "_" + toSnackCaseFromCamelCase( existsManyAssociation.qualifiedName().name() ) + "_cross_ref";

         final String name = existsManyAssociation.qualifiedName().name();
         final JSONArray array = entity.getJSONArray( name );
         for ( int i = 0; ; i++ )
         {
            final JSONObject arrEl = array.optJSONObject( i );
            if ( arrEl == null )
            {
               break;
            }

            final Class<?> assocClassName = Class.forName( existsManyAssociation.type() );
            int j = 0;
            String associationClass = null;
            for ( EntityInfo entityInfo : EntityInfo.values() )
            {
               if ( assocClassName.isAssignableFrom( entityInfo.getEntityClass() ) )
               {
                  associationClass = entityInfo.getEntityClass().getName();
                  j++;
               }
            }
            if ( j == 1 )
            {
               checkEntityExists( associationClass, arrEl.getString( "identity" ) );
            }

            final PreparedStatement preparedStatement = connection.prepareStatement( "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                    " (owner_id,link_id) VALUES (?,?)" );
            preparedStatement.setString( 1, entity.getString( "identity" ) );
            preparedStatement.setString( 2, arrEl.getString( "identity" ) );
            preparedStatement.executeUpdate();
            preparedStatement.close();
         }

      }


   }

   private void mainUpdate( String identity ) throws SQLException, JSONException, ClassNotFoundException
   {

      StringBuilder query = new StringBuilder( "UPDATE  " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " SET " );


      boolean hasProps = false;
      for ( PropertyType existsProperty : existsProperties )
      {
         final String name = existsProperty.qualifiedName().name();

         if ( !name.equals( "identity" ) && subProps.get( name ) == null )
         {
            query
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                    .append( " = ?," );

            hasProps = true;
         }
      }

      if ( hasProps )
      {
         query
                 .deleteCharAt( query.length() - 1 )
                 .append( " WHERE " )
                 .append( escapeSqlColumnOrTable( "identity" ) )
                 .append( " = ?" );


         final PreparedStatement statement = connection.prepareStatement( query.toString() );
         final int index = addArguments( statement );
         statement.setString( index, identity );
         statement.executeUpdate();
         statement.close();
      }
   }

   private String tableName()
   {
      return toSnackCaseFromCamelCase( classSimpleName( className ) );
   }

   private void saveSubProperties() throws Exception
   {
      final Set<String> keys = subProps.keySet();
      for ( String key : keys )
      {

         final Object value = subProps.get( key );

         StringBuilder query = new StringBuilder( "UPDATE  " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
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
                 .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( key ) ) )
                 .append( "= ?" )
                 .append( " WHERE " )
                 .append( escapeSqlColumnOrTable( "identity" ) )
                 .append( " = ?" );

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
                    escapeSqlColumnOrTable( tableName )+
                    " (" + escapeSqlColumnOrTable( "property_value" )+ ") VALUES (?)";

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
                    " (" + escapeSqlColumnOrTable( "property_key"  )+ "," + escapeSqlColumnOrTable( "property_value" )+ ") VALUES (?,?)";

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
         final JSONObject jsonObject = entity.getJSONObject( name );
         final String identity = jsonObject.getString( "identity" );

         final Class<?> assocClassName = Class.forName( existsAssociation.type().name() );
         String associationClass = null;
         for ( EntityInfo entityInfo : EntityInfo.values() )
         {
            if ( assocClassName.isAssignableFrom( entityInfo.getEntityClass() ) )
            {
               associationClass = entityInfo.getEntityClass().getName();
            }
         }

         checkEntityExists( jsonObject.optString( "_type", associationClass ), identity );
         associations.put( toSnackCaseFromCamelCase( name ), identity );
      }

      if ( associations.size() > 0 )
      {
         final Set<String> keys = associations.keySet();

         StringBuilder query = new StringBuilder( "UPDATE  " )
                 .append( escapeSqlColumnOrTable( tableName() ) )
                 .append( " SET " );

         for ( String key : keys )
         {
            query
                    .append( escapeSqlColumnOrTable( key ) )
                    .append( " = ?," );
         }
         query
                 .deleteCharAt( query.length() - 1 )
                 .append( " WHERE " )
                 .append( escapeSqlColumnOrTable( "identity" ) )
                 .append( " = ?" );

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

   private void checkEntityExists( String type, String identity ) throws Exception
   {
      final String tableName = toSnackCaseFromCamelCase( classSimpleName( type ) );
      final ResultSet resultSet = selectFromWhereId( tableName, identity );

      if ( !resultSet.next() )
      {
         final String qeury = "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                 " (" + escapeSqlColumnOrTable( "identity" ) + ")" + " VALUES (?)";
         final PreparedStatement preparedStatement = connection.prepareStatement( qeury );
         preparedStatement.setString( 1, identity );
         preparedStatement.executeUpdate();
         preparedStatement.close();
      }

   }

   private void deleteEntityAndRelations( String identity ) throws SQLException, ClassNotFoundException, JSONException
   {
      StringBuilder select = new StringBuilder( "SELECT " );

      //Delete sub properties
      boolean allow = false;
      int count = 0;
      for ( PropertyType property : allProperties )
      {
         if ( property.type().isValue()
                 || property.type().type().name().equals( List.class.getName() )
                 || property.type().type().name().equals( Set.class.getName() )
                 || property.type().type().name().equals( Map.class.getName() ) )
         {
            allow = true;
            select
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( property.qualifiedName().name() ) ) )
                    .append( "," );
            count++;
         }

      }

      if ( allow )
      {
         select
                 .deleteCharAt( select.length() - 1 )
                 .append( " FROM " )
                 .append( escapeSqlColumnOrTable( tableName() ) );

         final ResultSet resultSet = connection.prepareStatement( select.toString() ).executeQuery();

         for ( int i = 1; i <= count; i++ )
         {
            if ( !resultSet.next() )
            {
               break;
            }
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
               final String delete = "DELETE FROM " + escapeSqlColumnOrTable( split[0] ) + " WHERE id = ?";
               final PreparedStatement preparedStatement = connection.prepareStatement( delete );
               preparedStatement.setString( 1, split[1] );
               preparedStatement.executeUpdate();
               preparedStatement.close();
            }
         }
      }

      //Delete many associations
      for ( ManyAssociationType manyAssociation : allManyAssociations )
      {

         String tableName = tableName() + "_" + toSnackCaseFromCamelCase( manyAssociation.qualifiedName().name() ) + "_cross_ref";
         final String delete = "DELETE FROM " + tableName + " WHERE owner_id = ?";
         final PreparedStatement preparedStatement = connection.prepareStatement( delete );
         preparedStatement.setString( 1, identity );
         preparedStatement.executeUpdate();
         preparedStatement.close();

      }

      //Set main entity all columns to NULL except identity

      final StringBuilder queryNullUpdate = new StringBuilder( "UPDATE " )
              .append( tableName() )
              .append( " SET " );

      for ( PropertyType property : allProperties )
      {
         final String name = property.qualifiedName().name();
         if ( !name.equals( "identity" ) )
         {
            queryNullUpdate.append( toSnackCaseFromCamelCase( name ) )
                    .append( "=NULL," );
         }
      }

      for ( AssociationType association : allAssociations )
      {
         final String name = association.qualifiedName().name();
         if ( !name.equals( "identity" ) )
         {
            queryNullUpdate.append( toSnackCaseFromCamelCase( name ) )
                    .append( "=NULL," );
         }
      }

      final String query = queryNullUpdate
              .deleteCharAt( queryNullUpdate.length() - 1 )
              .toString();

      final PreparedStatement preparedStatement = connection.prepareStatement( query );

      preparedStatement.executeUpdate();
      preparedStatement.close();

//      deleteFromWhereId( IDENTITY_TABLE_NAME, identity );

   }

   private int addArguments( PreparedStatement statement ) throws JSONException, SQLException, ClassNotFoundException
   {
      int i = 1;
      for ( PropertyType existsProperty : existsProperties )
      {
         final Class type = Class.forName( existsProperty.type().type().name() );

         final String name = existsProperty.qualifiedName().name();

         if ( name.equals( "identity" ) || subProps.get( name ) != null )
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
      return i;
   }

   private ResultSet selectFromWhereId( String tableName, String id ) throws SQLException, JSONException
   {
      String isExistQuery = "SELECT " + escapeSqlColumnOrTable( "identity" ) +
              " FROM " + escapeSqlColumnOrTable( tableName ) +
              " WHERE " + escapeSqlColumnOrTable( "identity" ) + " = ?";

      final PreparedStatement isExistPS = connection
              .prepareStatement( isExistQuery );
      isExistPS.setString( 1, id );
      return isExistPS.executeQuery();
   }

   private String escapeSqlColumnOrTable( String name )
   {
      return ESCAPE_SQL + name + ESCAPE_SQL;
   }

   private String classSimpleName( String className )
   {
      return className.substring( className.lastIndexOf( "." ) + 1 );
   }

   private String toSnackCaseFromCamelCase( String str )
   {
      StringBuilder stringBuilder = new StringBuilder();

      //out of naming rules
      String x = str.replace( "DTO", "Dto" );

      for ( int i = 0; i < x.length(); i++ )
      {
         char ch = x.charAt( i );
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

   public void setAllManyAssociations( Set<ManyAssociationType> allManyAssociations )
   {
      this.allManyAssociations = new ArrayList<>( allManyAssociations );
   }

   public void setAllAssociations( Set<AssociationType> allAssociations )
   {
      this.allAssociations = new ArrayList<>( allAssociations );
   }

   public void setClassName( String className )
   {
      this.className = className;
   }
}
