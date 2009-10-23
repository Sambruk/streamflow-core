/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.organization;

import org.qi4j.api.injection.scope.Uses;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationView;

import javax.swing.*;

public class OrganizationsTabbedView
    extends JTabbedPane
{
    public OrganizationsTabbedView(@Uses OrganizationsAdminView orgsUsers,
                                   @Uses UsersAdministrationView userAdministration)
    {
        addTab(text(AdministrationResources.organizations_tab), orgsUsers);
        addTab(text(AdministrationResources.users_tab), userAdministration);
    }
}
