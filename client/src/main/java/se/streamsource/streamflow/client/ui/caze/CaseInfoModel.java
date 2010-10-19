/**
 *
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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.resource.caze.CaseValue;

/**
 * Model for the quick info about a case.
 */
public class CaseInfoModel
      implements Refreshable
{
   private CommandQueryClient client;

   CaseValue caseValue;

   public CaseInfoModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public CaseValue getInfo()
   {
      return caseValue;
   }

   public void refresh()
   {
      caseValue = client.query( "info", CaseValue.class );
   }
}