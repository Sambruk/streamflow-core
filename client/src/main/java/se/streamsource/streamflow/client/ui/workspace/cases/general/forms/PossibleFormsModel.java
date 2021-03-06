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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.util.LinkValueListModel;

public class PossibleFormsModel
   extends LinkValueListModel
{
   public FormDraftModel getFormDraftModel(String id)
   {
      CommandQueryClient possibleFormClient = client.getSubClient( id );

      possibleFormClient.query();

      if (!possibleFormClient.hasQueryWithRelation("formdraft")
            && !possibleFormClient.hasCommandWithRelation("create")) {
         return null;
      }

      if (!possibleFormClient.hasQueryWithRelation("formdraft")
            && possibleFormClient.hasCommandWithRelation("create")) {
         possibleFormClient.command("create");
         possibleFormClient.query();
      }

      if (possibleFormClient.hasQueryWithRelation("formdraft")) {
         LinkValue formDraftLink = possibleFormClient.queryByRelation("formdraft", LinkValue.class );
         CommandQueryClient formDraftClient = possibleFormClient.getClient( formDraftLink );
         return module.objectBuilderFactory().newObjectBuilder(FormDraftModel.class).use(formDraftClient).newInstance();
      }
      else {
         throw new RuntimeException("Formdraft unavailable after create. This shouldn't happen.");
      }
   }
}
