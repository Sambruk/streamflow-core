/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.label.LabelsView;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsView;
import se.streamsource.streamflow.client.ui.administration.roles.RolesView;
import se.streamsource.streamflow.client.ui.administration.tasktypes.TaskTypesAdminView;

import javax.swing.JTabbedPane;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

/**
 * JAVADOC
 */
public class OrganizationAdministrationView
      extends JTabbedPane
{
   public OrganizationAdministrationView( @Uses LabelsView labelsView,
                                          @Uses RolesView rolesView,
                                          @Uses TaskTypesAdminView taskTypesView,
                                          @Uses AdministratorsView administratorsAdmin
   )
   {
      addTab( text( AdministrationResources.labels_tab ), labelsView );
      addTab( text( AdministrationResources.tasktypes_tab ), taskTypesView );
//        addTab( text( AdministrationResources.roles_tab ), rolesView );
      addTab( text( AdministrationResources.administrators_tab ), administratorsAdmin );
   }
}