package se.streamsource.streamflow.client.ui.administration;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointsModel;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersModel;
import se.streamsource.streamflow.client.ui.administration.templates.SelectedTemplatesModel;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;

/**
 * Represents an organization in the administration model.
 */
public class OrganizationModel
   extends ResourceModel
{
   public OrganizationModel()
   {
      relationModelMapping("administrators", AdministratorsModel.class);
      relationModelMapping("labels", LabelsModel.class);
      relationModelMapping("selectedlabels", SelectedLabelsModel.class);
      relationModelMapping("organizationusers", OrganizationUsersModel.class);
      relationModelMapping("roles", RolesModel.class);
      relationModelMapping("forms", FormsModel.class);
      relationModelMapping("casetypes", CaseTypesModel.class);
      relationModelMapping("accesspoints", AccessPointsModel.class);
      relationModelMapping("emailaccesspoints", EmailAccessPointsModel.class);
      relationModelMapping("proxyusers", ProxyUsersModel.class);
      relationModelMapping("templates", SelectedTemplatesModel.class);
   }
}
