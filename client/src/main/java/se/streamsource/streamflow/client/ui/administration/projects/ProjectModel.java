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
package se.streamsource.streamflow.client.ui.administration.projects;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.filters.FiltersModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.projectsettings.CaseDueOnNotificationModel;
import se.streamsource.streamflow.client.ui.administration.projectsettings.RequiresCaseTypeModel;

/**
 * Represents a Project in the administration model
 */
public class ProjectModel
   extends ResourceModel
{
   public ProjectModel()
   {
      relationModelMapping("members", MembersModel.class);
      relationModelMapping("forms", FormsModel.class);
      relationModelMapping("casetypes", CaseTypesModel.class);
      relationModelMapping("labels", LabelsModel.class);
      relationModelMapping("selectedlabels", SelectedLabelsModel.class);
      relationModelMapping("selectedcasetypes", SelectedCaseTypesModel.class);
      relationModelMapping("filters", FiltersModel.class);
      relationModelMapping("caseaccessdefaults", CaseAccessDefaultsModel.class);
      relationModelMapping("dueonnotification", CaseDueOnNotificationModel.class);
      relationModelMapping("requirescasetype", RequiresCaseTypeModel.class);
   }
}
