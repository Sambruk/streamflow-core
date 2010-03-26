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
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.OpenProjectExistsException;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.DeleteContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitContext.Mixin.class)
public interface OrganizationalUnitContext
   extends DescribableContext, DeleteContext, Context
{
   public void move( EntityReferenceDTO moveValue ) throws ResourceException;
   public void merge( EntityReferenceDTO moveValue ) throws ResourceException;

   @SubContext
   AdministratorsContext administrators();

   @SubContext
   GroupsContext groups();

   @SubContext
   ProjectsContext projects();

   @SubContext
   FormsContext forms();

   @SubContext
   SelectedLabelsContext selectedlabels();

   @SubContext
   OrganizationalUnitsContext organizationalunits();

   abstract class Mixin
      extends ContextMixin
      implements OrganizationalUnitContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public void move( EntityReferenceDTO moveValue ) throws ResourceException
      {
         OrganizationalUnitRefactoring ou = context.role(OrganizationalUnitRefactoring.class);
         OrganizationalUnits toEntity = uowf.currentUnitOfWork().get( OrganizationalUnits.class, moveValue.entity().get().identity() );

         try
         {
            ou.moveOrganizationalUnit( toEntity );
         } catch (MoveOrganizationalUnitException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
         }
      }

      public void merge( EntityReferenceDTO moveValue ) throws ResourceException
      {
         OrganizationalUnitRefactoring ou = context.role(OrganizationalUnitRefactoring.class);
         OrganizationalUnit toEntity = uowf.currentUnitOfWork().get( OrganizationalUnit.class, moveValue.entity().get().identity() );

         try
         {
            ou.mergeOrganizationalUnit( toEntity );
         } catch (MergeOrganizationalUnitException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
         }
      }

      public void delete() throws ResourceException
      {
         OrganizationalUnitRefactoring ou = context.role(OrganizationalUnitRefactoring.class);

         try
         {
            ou.deleteOrganizationalUnit();

         } catch (OpenProjectExistsException pe)
         {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, pe.getMessage() );
         }
      }


      public AdministratorsContext administrators()
      {
         return subContext( AdministratorsContext.class );
      }

      public GroupsContext groups()
      {
         return subContext( GroupsContext.class );
      }

      public ProjectsContext projects()
      {
         return subContext( ProjectsContext.class );
      }

      public FormsContext forms()
      {
         return subContext( FormsContext.class );
      }

      public SelectedLabelsContext selectedlabels()
      {
         return subContext( SelectedLabelsContext.class );
      }

      public OrganizationalUnitsContext organizationalunits()
      {
         return subContext( OrganizationalUnitsContext.class );
      }
   }
}
