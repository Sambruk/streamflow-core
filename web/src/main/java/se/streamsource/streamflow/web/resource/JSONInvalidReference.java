/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource;

import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;
import org.json.JSONObject;

/**
 * JAVADOC
 */
public class JSONInvalidReference
   implements InvalidReferenceEventHandler
{
   public Object invalidGetMethod( Context context, String reference, Object object, String property, Info info )
   {
      if (object instanceof JSONObject)
      {
         return ((JSONObject)object).opt( property );
      }

      return null;
   }

   public boolean invalidSetMethod( Context context, String s, String s1, Info info )
   {
      return false;
   }

   public Object invalidMethod( Context context, String reference, Object object, String method, Info info )
   {
      return null;
   }
}
