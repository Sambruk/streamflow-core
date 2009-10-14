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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Uniform;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.resource.LabelsClientResource;
import se.streamsource.streamflow.client.resource.StreamFlowClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.search.SearchClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.UserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.UserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.UserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.UserWaitingForClientResource;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.task.TasksModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;

import java.io.IOException;
import java.util.Observable;

/**
 * JAVADOC
 */
public class AccountModel
        extends Observable
{
    @Structure ObjectBuilderFactory obf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    IndividualRepository individualRepository;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    Uniform client;

    @Uses
    Account account;

    private WorkspaceModel workspaceModel;
    private OverviewModel overviewModel;
    private AdministrationModel administrationModel;
    private SearchResultTableModel searchResults;
    private TasksModel tasksModel;

    public AccountSettingsValue settings()
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        Account acc = uow.get(account);
        try
        {
            return acc.accountSettings();
        } finally
        {
            uow.discard();
        }
    }

    public void updateSettings(AccountSettingsValue value) throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        uow.get(account).updateSettings(value);
        uow.complete();
        setChanged();
        notifyObservers();
    }

    public void register()
    {
        try
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            uow.get(account).register(client);
            uow.complete();
        } catch (Exception e)
        {
            throw new OperationException(AdministrationResources.could_not_register_user, e);
        }
        setChanged();
        notifyObservers();
    }

    public String test() throws IOException, ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            return uow.get(account).server(client).version();
        } finally
        {
            uow.discard();
        }
    }

    public boolean isRegistered()
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            return uow.get(account).isRegistered();
        } finally
        {
            uow.discard();
        }
    }

    public UserClientResource userResource()
    {
        UnitOfWork uow = uowf.newUnitOfWork();

        try
        {
            return uow.get(account).user(client);
        } finally
        {
            uow.discard();
        }
    }

    public StreamFlowClientResource serverResource()
    {
        UnitOfWork uow = uowf.newUnitOfWork();

        try
        {
            return uow.get(account).server(client);
        } finally
        {
            uow.discard();
        }
    }

    public TreeValue organizations() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        Account acc = uow.get(account);
        try
        {
            if (acc.isRegistered())
            {
                return acc.user(client).administration().organizations();
            } else
            {
                return vbf.newValue(TreeValue.class);
            }
        } finally
        {
            uow.discard();
        }
    }

    public void remove() throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        Account acc = uow.get(account);
        individualRepository.individual().removeAccount(acc);
        uow.complete();
    }

    public void changePassword(ChangePasswordCommand changePasswordCommand) throws Exception
    {
        UnitOfWork uow = uowf.newUnitOfWork();

        try
        {
            Account account1 = uow.get(account);
            account1.changePassword(client, changePasswordCommand);
            setChanged();
            notifyObservers();
            uow.complete();
        } catch (Exception ex)
        {
            uow.discard();
            throw ex;
        }
    }

    public TasksModel tasks()
    {
        if (tasksModel == null)
            tasksModel = obf.newObjectBuilder( TasksModel.class ).use( this, serverResource().tasks() ).newInstance();

        return tasksModel;
    }

    public WorkspaceModel workspace()
    {
        if (workspaceModel == null)
        {
            UserClientResource resource = userResource();
            UserInboxClientResource userInboxResource = resource.workspace().user().inbox();
            UserAssignmentsClientResource userAssignmentsResource = resource.workspace().user().assignments();
            UserDelegationsClientResource userDelegationsResource = resource.workspace().user().delegations();
            UserWaitingForClientResource userWaitingForResource = resource.workspace().user().waitingFor();
            LabelsClientResource labelsResource = resource.workspace().user().labels();

            workspaceModel = obf.newObjectBuilder( WorkspaceModel.class ).use( this,
                    resource,
                    userInboxResource,
                    userAssignmentsResource,
                    userDelegationsResource,
                    userWaitingForResource,
                    labelsResource,
                    tasks() ).newInstance();
        }

        return workspaceModel;
    }

    public OverviewModel overview()
    {
        if (overviewModel == null)
            overviewModel = obf.newObjectBuilder( OverviewModel.class ).use( this, tasks(), userResource().overview() ).newInstance();

        return overviewModel;
    }

    public SearchResultTableModel search()
    {
        if (searchResults == null)
        {
            SearchClientResource search = userResource().search();
            searchResults = obf.newObjectBuilder(SearchResultTableModel.class).use(search, tasks()).newInstance();
        }

        return searchResults;
    }

    public AdministrationModel administration()
    {
        if (administrationModel == null)
            administrationModel = obf.newObjectBuilder( AdministrationModel.class ).use( this, tasks() ).newInstance();

        return administrationModel;
    }
}
