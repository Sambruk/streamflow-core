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
package se.streamsource.streamflow.client.ui.administration.casetypes;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.*;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;

/**
 * Represents a Case Type in the administration model
 */
public class RemovedCaseTypeModel
   extends ResourceModel
{
   public RemovedCaseTypeModel()
   {
       relationModelMapping( "casetypedetail", CaseTypeDetailModel.class);
       relationModelMapping("archival", CaseArchivalSettingModel.class);
   }
}
