/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;

/**
 * JAVADOC
 */
@Mixins(OrganizationContext.Mixin.class)
public interface OrganizationContext
   extends DescribableContext, Context
{
   @SubContext
   AdministratorsContext administrators();

   @SubContext
   GroupsContext groups();

   @SubContext
   LabelsContext labels();

   @SubContext
   OrganizationalUnitsContext organizationalunits();

   @SubContext
   OrganizationUsersContext users();

   @SubContext
   ProjectsContext projects();

   @SubContext
   RolesContext roles();

   @SubContext
   SelectedLabelsContext selectedlabels();

   @SubContext
   TaskTypesContext tasktypes();

   abstract class Mixin
      extends ContextMixin
      implements OrganizationContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public AdministratorsContext administrators()
      {
         return subContext( AdministratorsContext.class );
      }

      public GroupsContext groups()
      {
         return subContext( GroupsContext.class );
      }

      public LabelsContext labels()
      {
         return subContext(LabelsContext.class);
      }

      public OrganizationalUnitsContext organizationalunits()
      {
         return subContext( OrganizationalUnitsContext.class );
      }


      public OrganizationUsersContext users()
      {
         return subContext( OrganizationUsersContext.class );
      }

      public ProjectsContext projects()
      {
         return subContext( ProjectsContext.class );
      }

      public RolesContext roles()
      {
         return subContext( RolesContext.class );
      }

      public SelectedLabelsContext selectedlabels()
      {
         return subContext( SelectedLabelsContext.class );
      }

      public TaskTypesContext tasktypes()
      {
         return subContext( TaskTypesContext.class );
      }

   }
}
