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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.project.MemberValue;
import se.streamsource.streamflow.web.domain.project.Members;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/members
 */
public class MembersServerResource
        extends CommandQueryServerResource
{
    public ListValue members()
    {
        String identity = getRequest().getAttributes().get("project").toString();
        UnitOfWork uow = uowf.currentUnitOfWork();
        Members.MembersState members = uow.get(Members.MembersState.class, identity);

        ListValueBuilder builder = new ListValueBuilder(vbf);
        for (MemberValue member : members.members().get().members().get())
        {
            Participant participant = uow.get(Participant.class, member.participant().get().identity());
            builder.addListItem(participant.getDescription(), EntityReference.getEntityReference(participant));
        }
        return builder.newList();
    }

    public TreeValue memberRoles()
    {
        String identity = getRequest().getAttributes().get("project").toString();
        UnitOfWork uow = uowf.currentUnitOfWork();
        Members.MembersState members = uow.get(Members.MembersState.class, identity);

        ValueBuilder<TreeValue> builder = vbf.newValueBuilder(TreeValue.class);
        for (MemberValue member : members.members().get().members().get())
        {
            ValueBuilder<TreeNodeValue> memberNodebuilder = vbf.newValueBuilder(TreeNodeValue.class);
            Participant participant = uow.get(Participant.class, member.participant().get().identity());
            memberNodebuilder.prototype().description().set(participant.getDescription());
            memberNodebuilder.prototype().entity().set(EntityReference.getEntityReference(participant));

            /*
            // Create role nodes
            ValueBuilder<TreeNodeValue> roleNodebuilder = vbf.newValueBuilder(TreeNodeValue.class);
            for (EntityReference roleReference : member.roles().get())
            {
                Describable role = uow.get(Describable.class, roleReference.identity());
                roleNodebuilder.prototype().description().set(role.getDescription());
                roleNodebuilder.prototype().entity().set(roleReference);
                memberNodebuilder.prototype().children().get().add(roleNodebuilder.newInstance());
            }*/

            builder.prototype().roots().get().add(memberNodebuilder.newInstance());
        }

        return builder.newInstance();
    }
}