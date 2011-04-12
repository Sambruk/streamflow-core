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

import org.qi4j.api.query.*;
import org.qi4j.api.util.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.context.surface.endusers.*;
import se.streamsource.streamflow.web.domain.entity.caze.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;

import java.util.*;

/**
 * TODO
 */
public class ClosedCasesResource
   extends CommandQueryResource
   implements SubResources
{
   public ClosedCasesResource()
   {
      super(ClosedCasesContext.class);
   }

   public void cases() throws Throwable
   {
      Query<CaseEntity> closedCases = (Query<CaseEntity>) invoke();

      TableQuery query = (TableQuery) getArguments()[0];

      TableBuilder builder = new TableBuilder(module.valueBuilderFactory());

      String select = query.select();
      if (select.equals("*"))
         select = "description,created,closed,caseid,project,resolution,href";

      builder.selectedColumns(select, new String[][]
              {
                      {"description", "Description", "string"},
                      {"created", "Created", "date"},
                      {"closed", "Closed", "date"},
                      {"caseid", "Case Id", "string"},
                      {"project", "Project", "string"},
                      {"resolution", "Resolution", "string"},
                      {"href", "Href", "string"},
              });

      String[] columns = select.split("[ ,]");

      for (CaseEntity closedCase : closedCases)
      {
         Owner owner = closedCase.owner().get();
         String project = ((Describable) owner).getDescription();

         builder.row();

         for (String column : columns)
         {
            if (column.equals("description"))
               builder.cell(closedCase.description().get(), closedCase.description().get());
            else if (column.equals("created"))
               builder.cell(closedCase.createdOn().get(), DateFunctions.toUtcString(closedCase.createdOn().get()));
            else if (column.equals("closed"))
            {
               Date closed = closedCase.getHistoryMessage("closed").createdOn().get();
               builder.cell(closed, DateFunctions.toUtcString(closed));
            }
            else if (column.equals("caseid"))
               builder.cell(closedCase.caseId().get(), closedCase.caseId().get());
            else if (column.equals("project"))
               builder.cell(project, project);
            else if (column.equals("resolution"))
            {
               String resolution = null;
               if (closedCase.resolution().get() != null)
                  resolution = closedCase.resolution().get().getDescription();
               builder.cell(resolution, resolution);
            }
            else if (column.equals("href"))
               builder.cell(closedCase.toString() + "/", closedCase.toString() + "/");

         }
         builder.endRow();
      }
      
      result(builder.newTable());
   }

   public void resource(String segment) throws ResourceException
   {
      setResourceValidity( setRole( CaseEntity.class, segment ) );
      subResourceContexts(ClosedCaseContext.class);
   }
}
