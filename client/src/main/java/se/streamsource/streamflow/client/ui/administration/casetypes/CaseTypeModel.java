/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.casetypes;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseAccessDefaultsModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseArchivalSettingModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CaseDefaultDaysToCompleteModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.CasePrioritySettingModel;
import se.streamsource.streamflow.client.ui.administration.casesettings.FormOnCloseModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;

/**
 * Represents a Case Type in the administration model
 */
public class CaseTypeModel
   extends ResourceModel
{
   public CaseTypeModel()
   {
      relationModelMapping("forms", FormsModel.class);
      relationModelMapping("labels", LabelsModel.class);
      relationModelMapping("selectedforms", SelectedFormsModel.class);
      relationModelMapping("selectedlabels", SelectedLabelsModel.class);
      relationModelMapping("resolutions", ResolutionsModel.class);
      relationModelMapping("selectedresolutions", SelectedResolutionsModel.class);
      relationModelMapping("caseaccessdefaults", CaseAccessDefaultsModel.class);
      relationModelMapping("defaultdaystocomplete", CaseDefaultDaysToCompleteModel.class);
      relationModelMapping("archival", CaseArchivalSettingModel.class);
      relationModelMapping( "formonclose", FormOnCloseModel.class );
      relationModelMapping( "caseprioritysetting", CasePrioritySettingModel.class );
   }
}
