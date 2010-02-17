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

package se.streamsource.streamflow.web.resource.organizations.projects.members;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/members/{member}
 */
public class MemberServerResource
      extends CommandQueryServerResource
{
   public void putOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String memberId = getRequest().getAttributes().get( "member" ).toString();
      Member member = uow.get( Member.class, memberId );

      String id = getRequest().getAttributes().get( "project" ).toString();
      Project project = uow.get( Project.class, id );

      try
      {
         checkPermission( project );
      } catch (AccessControlException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
      }
      project.addMember( member );
   }

   public void deleteOperation() throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String memberId = getRequest().getAttributes().get( "member" ).toString();
      Member member = uow.get( Member.class, memberId );

      String id = getRequest().getAttributes().get( "project" ).toString();
      Project project = uow.get( Project.class, id );
      checkPermission( project );
      project.removeMember( member );
   }
}