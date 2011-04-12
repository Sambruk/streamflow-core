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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.resource.caze.*;

/**
 * List of contacts for a case
 */
public class CaseSubmittedFormsModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   EventList<SubmittedFormListDTO> submittedForms = new TransactionList<SubmittedFormListDTO>( new BasicEventList<SubmittedFormListDTO>( ) );

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "index", SubmittedFormsListDTO.class ).forms().get(), submittedForms );
   }

   public EventList<SubmittedFormListDTO> getSubmittedForms()
   {
      return submittedForms;
   }
}