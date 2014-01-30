/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.entity.organization;

import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * Visitor for the organizational structure
 */
public class OrganizationVisitor
{
   public boolean visitOrganization( Organization org)
   {
      return true;
   }

   public boolean visitOrganizationalUnit( OrganizationalUnit ou)
   {
      return true;
   }

   public boolean visitProject( Project project)
   {
      return true;
   }

   public boolean visitGroup( Group group)
   {
      return true;
   }

   public boolean visitCaseType( CaseType caseType)
   {
      return true;
   }

   public boolean visitSelectedCaseType( CaseType caseType )
   {
      return true;
   }

   public boolean visitForm( Form form)
   {
      return true;
   }

   public boolean visitSelectedForm( Form form )
   {
      return true;
   }

   public boolean visitLabel( Label label)
   {
      return true;
   }

   public boolean visitSelectedLabel( Label label )
   {
      return true;
   }

   public boolean visitResolution( Resolution resolution)
   {
      return true;
   }

   public boolean visitSelectedResolution( Resolution resolution )
   {
      return true;
   }
}
