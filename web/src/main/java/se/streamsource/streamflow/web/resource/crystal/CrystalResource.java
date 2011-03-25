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

package se.streamsource.streamflow.web.resource.crystal;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.web.context.crystal.CrystalContext;

/**
 * JAVADOC
 */
public class CrystalResource
   extends CommandQueryResource
{
   private static TableValue motionchart;

   public CrystalResource()
   {
      super( CrystalContext.class );
   }

   public void motionchart() throws Throwable
   {
      if (motionchart == null)
      {
         motionchart = (TableValue) invoke();
      }

      result(motionchart);
   }
}
