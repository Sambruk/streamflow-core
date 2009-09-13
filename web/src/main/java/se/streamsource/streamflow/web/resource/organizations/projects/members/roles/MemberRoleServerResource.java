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

package se.streamsource.streamflow.web.resource.organizations.projects.members.roles;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.Role;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/members/{member}/roles/{role}
 */
public class MemberRoleServerResource
        extends CommandQueryServerResource
{
    public void putOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Add role"));
        String member = getRequest().getAttributes().get("member").toString();
        Participant participant = uow.get(Participant.class, member);

        String roleId = getRequest().getAttributes().get("role").toString();
        Role role = uow.get(Role.class, roleId);

        String id = getRequest().getAttributes().get("project").toString();
        Project project = uow.get(Project.class, id);

        try
        {
            checkPermission(project);
        } catch(AccessControlException e)
        {
            uow.discard();
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }

        project.addRole(participant, role);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
            throw new ResourceException(e);
        }
    }

    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Remove role"));
        String member = getRequest().getAttributes().get("member").toString();
        Participant participant = uow.get(Participant.class, member);

        String id = getRequest().getAttributes().get("project").toString();
        Project project = uow.get(Project.class, id);

        String roleName = getRequest().getAttributes().get("role").toString();
        Role role = uow.get(Role.class, roleName);

        try
        {
            checkPermission(project);
        } catch(AccessControlException e)
        {
            uow.discard();
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }

        project.removeRole(participant, role);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
            throw new ResourceException(e);
        }
    }
}