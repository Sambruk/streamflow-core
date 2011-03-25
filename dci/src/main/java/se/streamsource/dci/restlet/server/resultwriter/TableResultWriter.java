/**
 *
 * Copyright 2009-2011 Streamsource AB
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
import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueComposite;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;
import se.streamsource.dci.value.table.CellValue;
import se.streamsource.dci.value.table.ColumnValue;
import se.streamsource.dci.value.table.RowValue;
import se.streamsource.dci.value.table.TableValue;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * JAVADOC
 */
public class TableResultWriter
      extends AbstractResultWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );
   private Template htmlTemplate;

   public TableResultWriter( @Service VelocityEngine velocity ) throws Exception
   {
      htmlTemplate = velocity.getTemplate( "rest/template/table.htm" );
   }

   public boolean write( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof TableValue)
      {
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
         if (MediaType.APPLICATION_JSON.equals( type ))
         {

               response.setEntity( new WriterRepresentation( MediaType.APPLICATION_JSON )
               {
                  @Override
                  public void write( Writer writer ) throws IOException
                  {
                     try
                     {
                        JSONWriter json = new JSONWriter(writer);
                        TableValue tableValue = (TableValue) result;

                        // Parse parameters
                        String tqx = response.getRequest().getResourceRef().getQueryAsForm().getFirstValue( "tqx" );
                        String reqId = null;
                        if (tqx != null)
                        {
                           String[] params = tqx.split( ";" );
                           for (String param : params)
                           {
                              String[] p = param.split( ":" );
                              String key = p[0];
                              String value = p[1];

                              if (key.equals( "reqId" ))
                                 reqId = value;
                           }
                        }

                        json.object().
                              key("version").value( "0.6" );
                        if (reqId != null)
                           json.key("reqId").value( reqId );
                        json.key( "status").value("ok" );

                        json.key( "table" ).object();

                        List<ColumnValue> columnList = tableValue.cols().get();
                        json.key( "cols" ).array();
                        for (ColumnValue columnValue : columnList)
                        {
                           json.object().
                              key("id").value( columnValue.id().get() ).
                              key("label").value(columnValue.label().get()).
                              key("type").value(columnValue.columnType().get()).
                           endObject();
                        }
                        json.endArray();

                        json.key( "rows" ).array();
                        for (RowValue rowValue : tableValue.rows().get())
                        {
                           json.object();
                           json.key( "c" ).array();
                           int idx = 0;
                           for (CellValue cellValue : rowValue.c().get())
                           {
                              json.object();
                              Object value = cellValue.v().get();
                              if (columnList.get( idx ).columnType().get().equals("date") && value != null)
                              {
                                 value = DateFunctions.toUtcString( (Date) value);
                              }
                              if (value != null)
                                 json.key( "v" ).value( value );
                              if (cellValue.f().get() != null)
                                 json.key("f").value( cellValue.f().get() );
                              json.endObject();

                              idx++;
                           }
                           json.endArray();
                           json.endObject();
                        }
                        json.endArray();
                        json.endObject();
                        json.endObject();
                     } catch (JSONException e)
                     {
                        throw new IOException( e );
                     }
                  }
               } );
               return true;
         } else if (MediaType.TEXT_HTML.equals( type ))
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
            rep.setCharacterSet( CharacterSet.UTF_8 );
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}
