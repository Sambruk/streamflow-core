package se.streamsource.streamflow.web.application.entityexport;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

      String isExistQuery = "SELECT identity FROM " +
              tableName() +
              " WHERE identity = ?";

      final PreparedStatement isExistPS = connection
              .prepareStatement( isExistQuery );
      isExistPS.setString( 1, entity.getString( "identity" ) );
      final ResultSet isExistRS = isExistPS.executeQuery();

      if ( isExistRS.next() )
      {
            deleteEntityWithRelation(isExistRS.getString( "identity" ));
      }


      StringBuilder mainInsert = new StringBuilder( "INSERT INTO " )
              .append( tableName() )
              .append( " " )
              .append( argumentsForEntityInsert( false ) )
              .append( " VALUES " )
              .append( argumentsForEntityInsert( true ) );

      final PreparedStatement statement = connection.prepareStatement( mainInsert.toString() );
      addArguments( statement );
      statement.executeUpdate();
      statement.close();

      saveAssociations();

      saveSubProperties();

   }

   private String tableName()
   {
      return toSnackCaseFromCamelCase(className.substring( className.lastIndexOf( "." ) ) );
   }

   private String toSnackCaseFromCamelCase(String str) {
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < str.length(); i++) {
         char ch = str.charAt(i);
         if (Character.isUpperCase(ch)) {
            stringBuilder.append('_');
            stringBuilder.append(Character.toLowerCase(ch));
         } else {
            stringBuilder.append(ch);
         }
      }
      return stringBuilder.toString();
   }

   private void saveSubProperties() throws Exception
   {
      final Set<String> keys = subProps.keySet();
      for ( String key : keys )
      {
         final ValueExportHelper valueExportHelper = ValueExportHelper.fromClass( Class.forName( className ) );
         valueExportHelper.setName( key );
         valueExportHelper.setValue( subProps.get( key ) );
         valueExportHelper.setConnection( connection );
         final String id = valueExportHelper.help();
         final String query = "INSERT INTO " + tableName() + " (" + toSnackCaseFromCamelCase( key ) + ") VALUES ('" + id + "')";
         PreparedStatement preparedStatement = connection.prepareStatement( query );
         preparedStatement.executeUpdate();
      }
   }

   private void saveAssociations() throws Exception
   {
      Map<String, String> associations = new LinkedHashMap<>();

      for ( AssociationType existsAssociation : existsAssociations )
      {
         final String name = existsAssociation.qualifiedName().name();
         final String identity = entity.getJSONObject( name ).getString( "identity" );
         associations.put( name, identity );
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
            associations.put( name, ( inMap == null ? "" : inMap + SEPARATOR ) + arrEl.getString( "identity" ) );
         }

      }

      final Set<String> keys = associations.keySet();

      StringBuilder query = new StringBuilder( "INSERT INTO " )
              .append( tableName() )
              .append( " (" );

      for ( String key : keys )
      {
         query.append( key ).append( "," );

      }
      query.deleteCharAt( query.length() - 1 );

      query.append( ") VALUES (" );

      for ( int i = 0; i < keys.size(); i++ )
      {
         query.append( "?," );
      }
      query.deleteCharAt( query.length() - 1 );
      query.append( ")" );

      final PreparedStatement preparedStatement = connection.prepareStatement( query.toString() );

      int i = 1;
      for ( String key : keys )
      {
         preparedStatement.setString( i++, associations.get( key ) );
      }

      preparedStatement.executeUpdate();
   }

   private void deleteEntityWithRelation( String identity ) throws SQLException
   {
      StringBuilder select = new StringBuilder( "SELECT (" );


      for ( PropertyType property : allProperties )
      {
         if ( property.qualifiedName().type().endsWith( "Value" ) )
         {
            select.append( property.qualifiedName().name() )
                    .append( "," );
         }

      }

      select.deleteCharAt( select.length() - 1 ).append( ") FROM " ).append( tableName() );

      final ResultSet resultSet = connection.prepareStatement( select.toString() ).executeQuery();

      for ( int i = 1; resultSet.next(); i++ )
      {
         final String name = allProperties.get( i ).qualifiedName().name();
         String id = resultSet.getString( name );

         String [] splitted;
         if ( id.contains( SEPARATOR ) ) {
            splitted = id.split( SEPARATOR );
         } else {
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


      final String delete = "DELETE FROM " + tableName() + " WHERE identity = ?";
      final PreparedStatement preparedStatement = connection.prepareStatement( delete );
      preparedStatement.setString( 1, identity );
      preparedStatement.executeQuery();
   }


   private void addArguments( PreparedStatement statement ) throws JSONException, SQLException, ClassNotFoundException
   {
      int i = 0;
      for ( PropertyType existsProperty : existsProperties )
      {
         final Class type = Class.forName( existsProperty.type().type().name() );

         if ( Boolean.class.equals( type ) )
         {
            statement.setBoolean( i++, entity.getBoolean( existsProperty.qualifiedName().name() ) );
         } else if ( Integer.class.equals( type ) )
         {
            statement.setInt( i++, entity.getInt( existsProperty.qualifiedName().name() ) );
         } else if ( Long.class.equals( type ) )
         {
            statement.setLong( i++, entity.getLong( existsProperty.qualifiedName().name() ) );
         } else if ( Float.class.equals( type ) )
         {
            statement.setFloat( i++, ( float ) entity.getDouble( existsProperty.qualifiedName().name() ) );
         } else if ( Double.class.equals( type ) )
         {
            statement.setDouble( i++, entity.getDouble( existsProperty.qualifiedName().name() ) );
         } else if ( String.class.equals( type ) || type.isEnum() )
         {
            statement.setString( i++, entity.getString( existsProperty.qualifiedName().name() ) );
         } else if ( Date.class.equals( type ) || DateTime.class.equals( type ) )
         {
            statement.setString( i++, entity.getString( existsProperty.qualifiedName().name() ) );
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
               temp.append( name )
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
