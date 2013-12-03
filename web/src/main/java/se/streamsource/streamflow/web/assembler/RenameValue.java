/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.assembler;

import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;

/**
 * For a particular entity property, rename a value type. Useful for package and class name changes.
 */
public class RenameValue
   implements EntityMigrationOperation
{
   private String propertyName;
   private String fromValueType;
   private String toValueType;

   public RenameValue(String propertyName, String fromValueType, String toValueType)
   {
      this.propertyName = propertyName;
      this.fromValueType = fromValueType;
      this.toValueType = toValueType;
   }

   public boolean upgrade(JSONObject state, StateStore stateStore, Migrator migrator) throws JSONException
   {
      JSONObject fieldValue = state.getJSONObject("properties").getJSONObject(propertyName);
      if (fieldValue.get("_type").equals(fromValueType))
      {
         fieldValue.put("_type", toValueType);
         return true;
      }

      return false;
   }

   public boolean downgrade(JSONObject state, StateStore stateStore, Migrator migrator) throws JSONException
   {
      JSONObject fieldValue = state.getJSONObject("properties").getJSONObject(propertyName);
      if (fieldValue.get("_type").equals(toValueType))
      {
         fieldValue.put("_type", fromValueType);
         return true;
      }

      return false;
   }
}
