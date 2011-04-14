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

package se.streamsource.streamflow.web.resource.surface.endusers;

import org.qi4j.api.util.DateFunctions;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.context.surface.endusers.OpenCasesContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;

/**
 * TODO
 */
public class OpenCasesResource
        extends CommandQueryResource
        implements SubResources
{
   public OpenCasesResource()
   {
      super(OpenCasesContext.class);
   }

   public void cases() throws Throwable
   {
      Iterable<CaseEntity> openCases = (Iterable<CaseEntity>) invoke();

      TableQuery query = (TableQuery) getArguments()[0];

      TableBuilder builder = new TableBuilder(module.valueBuilderFactory());

      String select = query.select();
      if (select.equals("*"))
         select = "description,created,caseid,status,project,href";

      builder.selectedColumns(select, new String[][]
              {
                      {"description", "Description", "string"},
                      {"created", "Created", "date"},
                      {"caseid", "Case Id", "string"},
                      {"status", "Status", "string"},
                      {"project", "Project", "string"},
                      {"href", "Href", "string"},
              });

      String[] columns = select.split("[ ,]");

      for (CaseEntity openCase : openCases)
      {
         Owner owner = openCase.owner().get();
         String project = ((Describable) owner).getDescription();

         builder.row();

         for (String column : columns)
         {
            if (column.equals("description"))
               builder.cell(openCase.description().get(), openCase.description().get());
            else if (column.equals("created"))
               builder.cell(openCase.createdOn().get(), DateFunctions.toUtcString(openCase.createdOn().get()));
            else if (column.equals("caseid"))
               builder.cell(openCase.caseId().get(), openCase.caseId().get());
            else if (column.equals("status"))
               builder.cell(openCase.status().get().name(), openCase.status().get().name());
            else if (column.equals("project"))
               builder.cell(project, project);
            else if (column.equals("href"))
               builder.cell(openCase.toString() + "/", openCase.toString() + "/");

         }
         builder.endRow();
      }

      result(builder.newTable());
   }

   public void resource(String segment) throws ResourceException
   {
      setResourceValidity(setRole(CaseEntity.class, segment));
      subResource(OpenCaseResource.class);
   }
}
