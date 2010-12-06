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

package se.streamsource.dci.restlet.server.resultwriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueComposite;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;
import se.streamsource.dci.value.CellValue;
import se.streamsource.dci.value.ColumnValue;
import se.streamsource.dci.value.RowValue;
import se.streamsource.dci.value.TableValue;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * JAVADOC
 */
public class TableResultWriter
      extends AbstractResultWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_HTML );
   private Template htmlTemplate;

   public TableResultWriter( @Service VelocityEngine velocity ) throws Exception
   {
      htmlTemplate = velocity.getTemplate( "rest/template/table.htm" );
   }

   public boolean write( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof TableValue)
      {
         MediaType type = getVariant(response.getRequest(), ENGLISH, supportedMediaTypes).getMediaType();
         if (MediaType.APPLICATION_JSON.equals(type))
         {
            try
            {
               TableValue tableValue = (TableValue) result;

               // Parse parameters
               String tqx = response.getRequest().getResourceRef().getQueryAsForm(  ).getFirstValue( "tqx" );
               String reqId = null;
               if (tqx != null)
               {
                  String[] params = tqx.split( ";" );
                  for (String param : params)
                  {
                     String[] p = param.split( ":" );
                     String key = p[0];
                     String value = p[1];

                     if (key.equals("reqId"))
                        reqId = value;
                  }
               }

               final JSONObject responseJson = new JSONObject();
               responseJson.put( "version", "0.6" );
               if (reqId != null)
                  responseJson.put("reqId", reqId);
               responseJson.put( "status", "ok" );

               JSONObject dataTable = new JSONObject();

               JSONArray cols = new JSONArray();
               List<ColumnValue> columnList = tableValue.columns().get();
               for (ColumnValue columnValue : columnList)
               {
                  JSONObject column = new JSONObject();
                  column.put( "id", columnValue.id().get() );
                  column.put( "label", columnValue.label().get() );
                  column.put( "type", columnValue.columnType().get() );
                  cols.put( column );
               }
               dataTable.put( "cols", cols );

               JSONArray rows = new JSONArray();
               for (RowValue rowValue : tableValue.rows().get())
               {
                  JSONObject row = new JSONObject();
                  JSONArray cells = new JSONArray();
                  int idx = 0;
                  for (CellValue cellValue : rowValue.cells().get())
                  {
                     JSONObject cell = new JSONObject();
                     cell.putOpt( "v", cellValue.value().get() );
                     cell.putOpt( "f", cellValue.formatted().get() );
                     cells.put( cell );
                     
                     idx++;
                  }
                  row.put( "c", cells );
                  rows.put( row );
               }
               dataTable.put( "rows", rows );

               responseJson.put( "table", dataTable );

               response.setEntity( new WriterRepresentation( MediaType.APPLICATION_JSON )
               {
                  @Override
                  public void write( Writer writer ) throws IOException
                  {
                     try
                     {
                        responseJson.write( writer );
                     } catch (JSONException e)
                     {
                        throw new IOException( e );
                     }
                  }
               } );
               return true;
            } catch (JSONException e)
            {
               throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not serialize table to JSON");
            }
         } else if (MediaType.TEXT_HTML.equals(type))
         {
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", new ValueCompositeContext( (ValueComposite) result ) );
                  htmlTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}
