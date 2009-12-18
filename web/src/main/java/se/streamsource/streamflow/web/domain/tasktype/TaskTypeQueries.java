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
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.organization.Organization;
import se.streamsource.streamflow.web.domain.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.project.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.user.User;

/**
 * JAVADOC
 */

@Mixins(TaskTypeQueries.Mixin.class)
public interface TaskTypeQueries
{
   ListValue taskTypes();

   ListValue possibleProjects();

   ListValue possibleUsers();

   class Mixin
         implements TaskTypeQueries
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Ownable.Data ownable;

      @This
      TypedTask.Data typedTask;

      public ListValue taskTypes()
      {
         Owner owner = ownable.owner().get();
         if (owner instanceof OrganizationParticipations)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) owner;

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
            Organization org = ((OwningOrganization) ou).organization().get();
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
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) owner;

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
            Organization org = ((OwningOrganization) ou).organization().get();

            return org.possibleProjects( typedTask.taskType().get() );
         } else
         {
            return vbf.newValue( ListValue.class );
         }
      }

      public ListValue possibleUsers()
      {
         Owner owner = ownable.owner().get();
         if (owner instanceof OrganizationParticipations)
         {
            OrganizationParticipations.Data orgs = (OrganizationParticipations.Data) owner;

            ListValueBuilder lvb = new ListValueBuilder(vbf);

            for (Organization organization : orgs.organizations())
            {
               addUsers( lvb, organization );
            }

            return lvb.newList();
         } else if (owner instanceof OwningOrganizationalUnit.Data)
         {
            OwningOrganizationalUnit.Data ouOwner = (OwningOrganizationalUnit.Data) owner;
            OrganizationalUnit ou = ouOwner.organizationalUnit().get();
            Organization org = ((OwningOrganization) ou).organization().get();

            ListValueBuilder lvb = new ListValueBuilder(vbf);

            addUsers(lvb, org);

            return lvb.newList();
         } else
         {
            return vbf.newValue( ListValue.class );
         }
      }

      private void addUsers( ListValueBuilder lvb, Organization organization )
      {
         OrganizationParticipations.Data userOrgs = QueryExpressions.templateFor(OrganizationParticipations.Data.class);
         Query<User> query = qbf.newQueryBuilder( User.class ).where( QueryExpressions.contains(userOrgs.organizations(), organization )).newQuery( uowf.currentUnitOfWork() );

         for (User user : query)
         {

            String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
            lvb.addDescribable( user, group );
         }
      }
   }
}
