package se.streamsource.streamflow.client.ui.administration.casetypes;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.caseaccessdefaults.CaseAccessDefaultsModel;
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
      //TODO Archival
   }
}
