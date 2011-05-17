package se.streamsource.streamflow.client.ui.administration;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;

/**
 * Represents an organizational unit in the administration model.
 */
public class OrganizationalUnitModel
      extends ResourceModel
{
   public OrganizationalUnitModel()
   {
      relationModelMapping("administrators", AdministratorsModel.class);
      relationModelMapping("groups", GroupsModel.class);
      relationModelMapping("projects", ProjectsModel.class);
      relationModelMapping("forms", FormsModel.class);
      relationModelMapping("casetypes", CaseTypesModel.class);
      relationModelMapping("labels", LabelsModel.class);
      relationModelMapping("selectedlabels", SelectedLabelsModel.class);
   }
}
