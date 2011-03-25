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

package se.streamsource.streamflow.client.util;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.caseaccessdefaults.CaseAccessDefaultsView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesView;
import se.streamsource.streamflow.client.ui.administration.forms.FormsView;
import se.streamsource.streamflow.client.ui.administration.forms.SelectedFormsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormEditView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormElementsView;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesView;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsView;
import se.streamsource.streamflow.client.ui.administration.labels.LabelsView;
import se.streamsource.streamflow.client.ui.administration.labels.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.organizations.OrganizationUsersView;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsView;
import se.streamsource.streamflow.client.ui.administration.projects.MembersView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsView;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.EmailAccessPointsView;
import se.streamsource.streamflow.client.ui.administration.surface.ProxyUsersView;
import se.streamsource.streamflow.client.ui.administration.templates.TemplatesView;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationView;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.dci.value.link.Links.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * Show a REST resource as a tabbed view. To determine tabs, do a query to the resources directory URL ("/") to get
 * a ResourceValue. Then iterate through the registered views and check if they are in ResourceValue.resources(). By using
 * the order of the registered views we ensure that the UI order of the tabs is always the same, regardless of the order
 * returned from the server.
 */
public class TabbedResourceView
      extends JTabbedPane
{
   private static final Map<String, Class<? extends JComponent>> views = new LinkedHashMap<String, Class<? extends JComponent>>();
   private static final Map<String, Enum> tabNames = new LinkedHashMap<String, Enum>();

   static
   {
      addTab( "users", AdministrationResources.users_tab, UsersAdministrationView.class );

      addTab( "members", AdministrationResources.members_tab, MembersView.class );
      addTab( "projects", AdministrationResources.projects_tab, ProjectsView.class );
      addTab( "groups", AdministrationResources.groups_tab, GroupsView.class );
      addTab( "forms", AdministrationResources.forms_tab, FormsView.class );
      addTab( "selectedforms", AdministrationResources.selected_forms_tab, SelectedFormsView.class );

      addTab( "casetypes", AdministrationResources.casetypes_tab, CaseTypesView.class );
      addTab( "selectedcasetypes", AdministrationResources.selected_casetypes_tab, SelectedCaseTypesView.class );

      addTab( "labels", AdministrationResources.labels_tab, LabelsView.class );
      addTab( "selectedlabels", AdministrationResources.selected_labels_tab, SelectedLabelsView.class );

      addTab( "resolutions", AdministrationResources.resolutions_tab, ResolutionsView.class );
      addTab( "selectedresolutions", AdministrationResources.selected_resolutions_tab, SelectedResolutionsView.class );

      addTab( "organizationusers", AdministrationResources.users_tab, OrganizationUsersView.class );

      addTab( "accesspoints", AdministrationResources.accesspoints_tab, AccessPointsView.class );
      addTab( "emailaccesspoints", AdministrationResources.emailaccesspoints_tab, EmailAccessPointsView.class );
      addTab( "proxyusers", AdministrationResources.proxyusers_tab, ProxyUsersView.class );
      addTab( "attachments", AdministrationResources.templates_tab, TemplatesView.class );

      addTab( "forminfo", AdministrationResources.forminfo_tab, FormEditView.class );
      addTab( "pages", AdministrationResources.formpages_tab, FormElementsView.class );
      addTab( "signatures", AdministrationResources.formsignatures_tab, FormSignaturesView.class );

      addTab( "caseaccessdefaults", AdministrationResources.caseaccessdefaults_tab, CaseAccessDefaultsView.class );

      addTab( "administrators", AdministrationResources.administrators_tab, AdministratorsView.class );
   }

   private static void addTab( String name, Enum tabName, Class<? extends JComponent> viewClass )
   {
      tabNames.put( name, tabName );
      views.put( name, viewClass );
   }

   public TabbedResourceView( @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      setTabLayoutPolicy( JTabbedPane.WRAP_TAB_LAYOUT );

      ResourceValue resource = client.queryResource();
      List<LinkValue> resources = resource.resources().get();
      int index = 0;
      for (Map.Entry<String, Class<? extends JComponent>> stringClassEntry : views.entrySet())
      {
         if (matchesAny( withRel( stringClassEntry.getKey() ), resources ))
         {
            String tabNameText = text( tabNames.get( stringClassEntry.getKey() ) );
            Class<? extends JComponent> tabClass = stringClassEntry.getValue();
            addTab( tabNameText, obf.newObjectBuilder( tabClass ).use( client.getSubClient( stringClassEntry.getKey() ) ).newInstance() );
            setMnemonicAt( index, KeyEvent.VK_1 + index );
            index++;
         }
      }
   }
}
