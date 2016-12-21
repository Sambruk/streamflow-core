/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormListDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormsListDTO;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * List of contacts for a case
 */
public class CaseSubmittedFormsModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   EventList<SubmittedFormListDTO> submittedForms = new TransactionList<SubmittedFormListDTO>( new BasicEventList<SubmittedFormListDTO>( ) );

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "index", SubmittedFormsListDTO.class ).forms().get(), submittedForms );
   }

   public EventList<SubmittedFormListDTO> getSubmittedForms()
   {
      return submittedForms;
   }

   public CaseSubmittedFormModel newSubmittedFormModel(int idx)
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseSubmittedFormModel.class).use( client, new Integer(idx) ).newInstance();
   }
}