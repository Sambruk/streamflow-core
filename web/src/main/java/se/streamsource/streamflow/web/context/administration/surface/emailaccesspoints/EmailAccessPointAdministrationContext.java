/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.surface.EmailAccessPointDTO;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.organization.EmailAccessPointEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

import java.io.*;
import java.util.*;

import static org.qi4j.api.query.QueryExpressions.*;
import static se.streamsource.dci.api.RoleMap.*;

/**
 * TODO
 */
public class EmailAccessPointAdministrationContext
        implements IndexContext<EmailAccessPointDTO>, DeleteContext
{
   @Structure
   Module module;

   public void delete() throws ResourceException, IOException
   {
      role(EmailAccessPoints.class).removeEmailAccessPoint(role(EmailAccessPoint.class));
   }

   public Iterable<? extends Project> possibleprojects()
   {
      return module.queryBuilderFactory().newQueryBuilder(ProjectEntity.class).
              where(eq(templateFor(Removable.Data.class).removed(), false)).
              newQuery(module.unitOfWorkFactory().currentUnitOfWork());
   }

   public void changeproject(@Name("entity") Project project)
   {
      role(AccessPointSettings.class).changedProject(project);
   }

   public Iterable<CaseType> possiblecasetypes()
   {
      Project project = RoleMap.role( AccessPointSettings.Data.class ).project().get();

      if (project != null)
      {
         SelectedCaseTypes.Data data = (SelectedCaseTypes.Data) project;
         return data.selectedCaseTypes();
      } else
         return Collections.emptyList();
   }

   public void changecasetype(@Name("entity") CaseType caseType)
   {
      role(AccessPointSettings.class).changedCaseType(caseType);
   }

   public void changesubject(@Name("subject") String subject)
   {
      role(EmailTemplates.class).changeSubject(subject);
   }

   public void changetemplate(@Name("key") String key, @Name("template") String template)
   {
      role(EmailTemplates.class).changeTemplate(key, template);
   }

   public EmailAccessPointDTO index()
   {
      EmailAccessPointEntity eap = role(EmailAccessPointEntity.class);

      ValueBuilder<EmailAccessPointDTO> builder = module.valueBuilderFactory().newValueBuilder(EmailAccessPointDTO.class);

      builder.prototype().subject().set(eap.subject().get());
      builder.prototype().email().set(eap.getDescription());
      CaseType caseType = eap.caseType().get();
      builder.prototype().caseType().set(caseType == null ? null : caseType.getDescription());
      Project project = eap.project().get();
      builder.prototype().project().set(project == null ? null : project.getDescription());

      builder.prototype().messages().set(eap.emailTemplates().get());

      return builder.newInstance();
   }
}
