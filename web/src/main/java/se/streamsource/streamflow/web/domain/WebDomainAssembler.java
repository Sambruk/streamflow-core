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
import se.streamsource.streamflow.web.domain.organization.OrganizationAssembler;
import se.streamsource.streamflow.web.domain.task.SharedTaskAssembler;
import se.streamsource.streamflow.web.domain.user.UserAssembler;

/**
 * JAVADOC
 */
public class WebDomainAssembler
{
    public void assemble(LayerAssembly domainLayer) throws AssemblyException
    {
        new OrganizationAssembler().assemble(domainLayer.newModuleAssembly("Organization"));
        new SharedTaskAssembler().assemble(domainLayer.newModuleAssembly("Task"));
        new UserAssembler().assemble(domainLayer.newModuleAssembly("User"));
        new CommentAssembler().assemble(domainLayer.newModuleAssembly("Comments"));
        new CommonResourceAssembler().assemble(domainLayer.newModuleAssembly("Common"));
    }
}
