package se.streamsource.streamflow.client.ui.administration.projects;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.caseaccessdefaults.CaseAccessDefaultsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;

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
      relationModelMapping("caseaccessdefaults", CaseAccessDefaultsModel.class);
   }
}
