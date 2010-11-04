/*
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.crystal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.value.TableBuilder;
import se.streamsource.dci.value.TableQuery;
import se.streamsource.dci.value.TableValue;
import se.streamsource.streamflow.web.infrastructure.database.Databases;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * JAVADOC
 */
public class CrystalContext
{
   @Structure
   Module module;

   @Service
   ServiceReference<DataSource> source;

   ResourceBundle sql;

   public CrystalContext()
   {
      ResourceBundle.clearCache();
      sql = ResourceBundle.getBundle( getClass().getPackage().getName()+".sql" );
   }

   public TableValue motionchart()
   {
      final TableBuilder tableBuilder = new TableBuilder( module.valueBuilderFactory() );
      tableBuilder.
            column( "CaseType", "Case type", "string" ).
            column( "Week", "Week", "string" ).
            column( "Variation", "Variation", "number" ).
            column( "Duration", "Duration", "number" ).
            column( "CaseCount", "Case count", "number" ).
            column( "CasetypeOwner", "Casetype owner", "string" );


      final Logger logger = LoggerFactory.getLogger( getClass() );
      try
      {
         final SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy'W'ww");

         Calendar[] range = findRange();
         logger.info( "From "+weekFormat.format( range[0].getTime()) +" to "+weekFormat.format(range[1].getTime()) );

         // Find cases for each week
         Databases databases = new Databases(source.get());
         final Calendar calCurrent = range[0];
         while (calCurrent.before( range[1] ))
         {
            final Calendar minWeek = (Calendar) calCurrent.clone();
            calCurrent.roll( Calendar.WEEK_OF_YEAR, 1 );
            final Calendar maxWeek = calCurrent;
            databases.query( sql.getString( "motionchart" ), new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement preparedStatement ) throws SQLException
               {
                  logger.info( "From "+weekFormat.format( minWeek.getTime())+" to "+weekFormat.format( maxWeek.getTime()) );
                  preparedStatement.setTimestamp( 1, new Timestamp(minWeek.getTimeInMillis()));
                  preparedStatement.setTimestamp( 2, new Timestamp(maxWeek.getTimeInMillis()));
               }
            },
                  new Databases.ResultSetVisitor()
                  {
                     public boolean visit( ResultSet visited ) throws SQLException
                     {
                        tableBuilder.row().
                           cell(visited.getString( "casetype" ), null).
                           cell(weekFormat.format( minWeek.getTime() ), "v"+minWeek.get( Calendar.WEEK_OF_YEAR )).
                           cell(visited.getString("variationpct"), null).
                           cell((visited.getLong( "average")/(1000*60*60))+"", null).
                           cell(visited.getString("count"), null).
                           cell(visited.getString("casetype_owner"), null);

                        return true;
                     }
                  });
         }

      } catch (SQLException e)
      {
         logger.warn( "Could not get statistics", e );
      }

      return tableBuilder.newTable();
   }

   public Representation timeline( final TableQuery query) throws Exception
   {
      final JSONObject timeline = new JSONObject();

      timeline.put( "date-time-format", "iso8601" );

      final JSONArray events = new JSONArray();

//      final Logger logger = LoggerFactory.getLogger( getClass() );
//      final SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy'W'ww");

 //     final Calendar[] range = findRange();

      Databases databases = new Databases(source.get());

      String sqlQuery = sql.getString("timeline");
      String where;
      if (query.where() != null)
         where = "where "+query.where();
      else
         where = "";

      String offset = "";
      if (query.offset() != null)
      {
         if (query.limit() != null)
         {
            offset = "limit "+query.offset()+","+query.limit();
         }
      }

      sqlQuery = MessageFormat.format( sqlQuery, where, offset );
      
      databases.query( sqlQuery, new Databases.ResultSetVisitor()
      {
         public boolean visit( ResultSet visited ) throws SQLException
         {
            try
            {
               JSONObject event = new JSONObject();
               event.put( "start", DateFunctions.toUtcString( new Date(visited.getTimestamp("created_on" ).getTime())));
               event.put( "end", DateFunctions.toUtcString( new Date(visited.getTimestamp("closed_on" ).getTime())));
               event.put( "title", visited.getString( "case_id" )+"("+visited.getString("casetype")+")");
               event.put( "description", visited.getString( "description" )+"("+visited.getString( "assigned" )+")");
               event.put( "durationEvent", "true");

               events.put(event);
            } catch (JSONException e)
            {
               LoggerFactory.getLogger( getClass() ).error( "Could not build JSON", e );
               return false;
            }

            return true;
         }
      });

      timeline.put("events", events);

      return new WriterRepresentation(MediaType.APPLICATION_JSON)
      {
         @Override
         public void write( Writer writer ) throws IOException
         {
            try
            {
               timeline.write( writer );
            } catch (JSONException e)
            {
               throw new IOException(e);
            }
         }
      };
   }

   public TableValue labelcloud() throws SQLException
   {
      Databases databases = new Databases(source.get());
      final TableBuilder builder = new TableBuilder(module.valueBuilderFactory());
      builder.column( "Name", "Name", "string" ).
            column("Count", "Count", "number");

      databases.query( sql.getString( "labelcloud" ), new Databases.ResultSetVisitor()
      {
         public boolean visit( ResultSet visited ) throws SQLException
         {
            builder.row().
                  cell(visited.getString( "description" ), null).cell( visited.getLong( "cnt"), null );

            return true;
         }
      });

      return builder.newTable();
   }

   private Calendar[] findRange() throws SQLException
   {
      Databases databases = new Databases(source.get());

      final Calendar[] range = new Calendar[2];

      // Find min/max dates
      databases.query( sql.getString("range"), new Databases.ResultSetVisitor()
      {
         public boolean visit( ResultSet visited ) throws SQLException
         {
            Calendar calMin = Calendar.getInstance();
            calMin.setTimeInMillis( visited.getTimestamp( 1 ).getTime() );
            calMin.clear( Calendar.DAY_OF_WEEK );

            Calendar calMax = Calendar.getInstance();
            calMax.setTimeInMillis( visited.getTimestamp( 2 ).getTime() );
            calMax.clear( Calendar.DAY_OF_WEEK );

            range[0] = calMin;
            range[1] = calMax;

            return false;
         }
      });

      return range;
   }
}
