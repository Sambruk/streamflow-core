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

package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.organization.MergeOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.MoveOrganizationalUnitException;
import se.streamsource.streamflow.domain.organization.OpenProjectExistsException;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * An organizational unit represents a part of an organization.
 */
@Mixins(OrganizationalUnitRefactoring.Mixin.class)
public interface OrganizationalUnitRefactoring
{
   void moveOrganizationalUnit( OrganizationalUnits to ) throws MoveOrganizationalUnitException;

   void mergeOrganizationalUnit( OrganizationalUnit to ) throws MergeOrganizationalUnitException;

   void deleteOrganizationalUnit() throws OpenProjectExistsException;

   interface Data
   {
      OrganizationalUnits getParent();
   }

   abstract class Mixin
         implements OrganizationalUnitRefactoring, Data
   {
      @This
      OrganizationalUnits.Data organizationalUnits;

      @This
      OrganizationalUnit organizationalUnit;

      @This
      OwningOrganization organization;


      public void deleteOrganizationalUnit() throws OpenProjectExistsException
      {
         if (((Projects.Data)organizationalUnit).projects().count() > 0)
         {
            throw new OpenProjectExistsException( "There are open projects" );
         } else
         {
            for (OrganizationalUnitRefactoring oue : organizationalUnits.organizationalUnits())
            {
               if (((Projects.Data)oue).projects().count() > 0)
               {
                  throw new OpenProjectExistsException( "There are open projects" );
               }
            }
         }
         OrganizationalUnits parent = getParent();
         parent.removeOrganizationalUnit( organizationalUnit );
         organizationalUnit.removeEntity();
      }

      public void moveOrganizationalUnit( OrganizationalUnits to ) throws MoveOrganizationalUnitException
      {
         OrganizationalUnits parent = getParent();
         if (organizationalUnit.equals( to ))
         {
            throw new MoveOrganizationalUnitException();
         }
         if (to.equals( parent ))
         {
            return;
         }

         parent.removeOrganizationalUnit( organizationalUnit );
         to.addOrganizationalUnit( organizationalUnit );
      }

      public void mergeOrganizationalUnit( OrganizationalUnit to ) throws MergeOrganizationalUnitException
      {
         OrganizationalUnits parent = getParent();
         if (organizationalUnit.equals( to ))
         {
            throw new MergeOrganizationalUnitException();
         }

         // Move stuff over to merged with OU
         organizationalUnit.mergeProjects( to );
         organizationalUnit.mergeGroups( to );
         organizationalUnit.mergeForms( to );
         organizationalUnit.mergeCaseTypes( to );
         organizationalUnit.mergeLabels( to );
         organizationalUnit.mergeSelectedLabels( to );
         organizationalUnit.mergeRolePolicy( to );

         parent.removeOrganizationalUnit( organizationalUnit );
         organizationalUnit.removeEntity();
      }

      public OrganizationalUnits getParent()
      {
         OrganizationalUnits.Data ous = (OrganizationalUnits.Data) organization.organization().get();
         return ous.getParent( organizationalUnit );
      }
   }
}
