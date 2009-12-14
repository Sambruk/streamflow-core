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

package se.streamsource.streamflow.web.domain;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import se.streamsource.streamflow.resource.CommonResourceAssembler;
import se.streamsource.streamflow.web.domain.comment.CommentAssembler;
import se.streamsource.streamflow.web.domain.form.FormAssembler;
import se.streamsource.streamflow.web.domain.group.GroupAssembler;
import se.streamsource.streamflow.web.domain.label.LabelAssembler;
import se.streamsource.streamflow.web.domain.organization.OrganizationAssembler;
import se.streamsource.streamflow.web.domain.project.ProjectAssembler;
import se.streamsource.streamflow.web.domain.role.RoleAssembler;
import se.streamsource.streamflow.web.domain.task.TaskAssembler;
import se.streamsource.streamflow.web.domain.tasktype.TaskTypeAssembler;
import se.streamsource.streamflow.web.domain.user.UserAssembler;

/**
 * Assemble the domain model in the web layer.
 */
public class WebDomainAssembler
{
   public void assemble( LayerAssembly domainLayer ) throws AssemblyException
   {
      new CommentAssembler().assemble( domainLayer.moduleAssembly( "Comments" ) );
      new CommonResourceAssembler().assemble( domainLayer.moduleAssembly( "Common" ) );
      new FormAssembler().assemble( domainLayer.moduleAssembly( "Forms" ) );
      new GroupAssembler().assemble( domainLayer.moduleAssembly( "Groups" ) );
      new LabelAssembler().assemble( domainLayer.moduleAssembly( "Labels" ) );
      new OrganizationAssembler().assemble( domainLayer.moduleAssembly( "Organizations" ) );
      new ProjectAssembler().assemble( domainLayer.moduleAssembly( "Projects" ) );
      new RoleAssembler().assemble( domainLayer.moduleAssembly( "Roles" ) );
      new TaskAssembler().assemble( domainLayer.moduleAssembly( "Tasks" ) );
      new TaskTypeAssembler().assemble( domainLayer.moduleAssembly( "Task types" ) );
      new UserAssembler().assemble( domainLayer.moduleAssembly( "Users" ) );
   }
}
