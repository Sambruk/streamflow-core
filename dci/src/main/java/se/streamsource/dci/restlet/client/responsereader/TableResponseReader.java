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

package se.streamsource.dci.restlet.client.responsereader;

import org.json.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.table.*;

/**
 * JAVADOC
 */
public class TableResponseReader
   implements ResponseReader
{
   @Structure
   ValueBuilderFactory vbf;

   public <T> T readResponse( Response response, Class<T> resultType ) throws ResourceException
   {
      if (response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON) && TableValue.class.isAssignableFrom( resultType ))
      {
         String jsonValue = response.getEntityAsText();
         try
         {
            JSONObject jsonObject = new JSONObject(jsonValue);

            JSONObject table = jsonObject.getJSONObject( "table" );
            TableBuilder builder = new TableBuilder(vbf);

            JSONArray cols = table.getJSONArray( "cols" );
            for (int i = 0; i < cols.length(); i++)
            {
               JSONObject col = cols.getJSONObject( i );
               builder.column( col.optString( "id" ),  col.getString( "label" ), col.getString( "type" ));
            }

            JSONArray rows = table.getJSONArray( "rows" );
            for (int i = 0; i < rows.length(); i++)
            {
               builder.row();
               JSONObject row = rows.getJSONObject( i );
               JSONArray cells = row.getJSONArray( "c" );
               for (int j = 0; j < cells.length(); j++)
               {
                  JSONObject cell = cells.getJSONObject( j );
                  Object value = cell.opt( "v" );
                  String formatted = cell.optString("f");

                  if (cols.getJSONObject( j ).getString( "type" ).equals("date") && value != null)
                     value = DateFunctions.fromString( value.toString() );

                  builder.cell( value, formatted );
               }
               builder.endRow();
            }

            return (T) builder.newTable();
         } catch (JSONException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
         }
      }

      return null;
   }
}
