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

package se.streamsource.streamflow.web.domain.tasktype;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.domain.organization.Organization;
import se.streamsource.streamflow.web.domain.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.project.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.Owner;

/**
 * JAVADOC
 */

@Mixins(TaskTypeQueries.Mixin.class)
public interface TaskTypeQueries
{
    ListValue taskTypes();

    ListValue possibleProjects();

    class Mixin
        implements TaskTypeQueries
    {
        @Structure
        ValueBuilderFactory vbf;

        @This
        Ownable.Data ownable;

        @This
        TypedTask.Data typedTask;

        public ListValue taskTypes()
        {
            Owner owner = ownable.owner().get();
            if (owner instanceof OrganizationParticipations)
            {
                OrganizationParticipations.Data orgs = (OrganizationParticipations.Data)owner;

                ValueBuilder<ListValue> builder = vbf.newValueBuilder( ListValue.class );

                for (Organization organization : orgs.organizations())
                {
                    builder.prototype().items().get().addAll( organization.taskTypeList().items().get() );
                }

                return builder.newInstance();
            } else if (owner instanceof OwningOrganizationalUnit.Data)
            {
                OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
                OrganizationalUnit ou = ouOwner.organizationalUnit().get();
                Organization org = ((OwningOrganization)ou).organization().get();
                return org.taskTypeList();
            } else
            {
                return vbf.newValue( ListValue.class );
            }
        }

        public ListValue possibleProjects()
        {
            Owner owner = ownable.owner().get();
            if (owner instanceof OrganizationParticipations)
            {
                OrganizationParticipations.Data orgs = (OrganizationParticipations.Data)owner;

                ValueBuilder<ListValue> builder = vbf.newValueBuilder( ListValue.class );

                for (Organization organization : orgs.organizations())
                {
                    builder.prototype().items().get().addAll( organization.possibleProjects( typedTask.taskType().get() ).items().get() );
                }

                return builder.newInstance();
            } else if (owner instanceof OwningOrganizationalUnit.Data)
            {
                OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
                OrganizationalUnit ou = ouOwner.organizationalUnit().get();
                Organization org = ((OwningOrganization)ou).organization().get();

                return org.possibleProjects( typedTask.taskType().get());
            } else
            {
                return vbf.newValue( ListValue.class );
            }
        }
    }
}
