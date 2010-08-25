/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints.AccessPointsContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.projects.ProjectsContext;
import se.streamsource.streamflow.web.context.surface.administration.organizations.proxyusers.ProxyUsersContext;

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
   LabelsContext labels();

   @SubContext
   OrganizationalUnitsContext organizationalunits();

   @SubContext
   OrganizationUsersContext users();

   @SubContext
   RolesContext roles();

   @SubContext
   SelectedLabelsContext selectedlabels();

   @SubContext
   FormsContext forms();

   @SubContext
   CaseTypesContext casetypes();

   @SubContext
   AccessPointsContext accesspoints();

   @SubContext
   ProxyUsersContext proxyusers();

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

      public FormsContext forms()
      {
         return subContext( FormsContext.class );
      }

      public CaseTypesContext casetypes()
      {
         return subContext( CaseTypesContext.class );
      }

            public AccessPointsContext accesspoints()
      {
         return subContext( AccessPointsContext.class );
      }

      public ProxyUsersContext proxyusers()
      {
         return subContext( ProxyUsersContext.class );
      }
   }
}
