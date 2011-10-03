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

package se.streamsource.streamflow.web.context.overview;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Function;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.api.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.web.domain.entity.user.OverviewQueries;

import java.io.IOException;
import java.util.Locale;

/**
 * JAVADOC
 */
public class OverviewContext
{
   @Structure
   Module module;

   @Uses
   Locale locale;

   @Uses OverviewQueries queries;

   public TableValue index(TableQuery tq)
   {
      return new TableBuilderFactory(module.valueBuilderFactory()).
            column("description", "Description", TableValue.STRING, new Function<ProjectSummaryDTO, Object>()
            {
               public Object map(ProjectSummaryDTO projectSummaryDTO)
               {
                  return projectSummaryDTO.description().get();
               }
            }).
            column("href", "Location", TableValue.STRING, new Function<ProjectSummaryDTO, Object>()
            {
               public Object map(ProjectSummaryDTO projectSummaryDTO)
               {
                  return projectSummaryDTO.identity().get()+"/";
               }
            }).
            column("inbox", "Inbox count", TableValue.STRING, new Function<ProjectSummaryDTO, Object>()
            {
               public Object map(ProjectSummaryDTO projectSummaryDTO)
               {
                  return projectSummaryDTO.inboxCount().get();
               }
            }).
            column("assignments", "Assignment count", TableValue.STRING, new Function<ProjectSummaryDTO, Object>()
            {
               public Object map(ProjectSummaryDTO projectSummaryDTO)
               {
                  return projectSummaryDTO.assignedCount().get();
               }
            }).
            newInstance(tq).rows(queries.getProjectsSummary()).orderBy().paging().newTable();
   }

   public Workbook generateexcelprojectsummary() throws IOException
   {
      final Workbook workbook = new HSSFWorkbook();

      queries.generateExcelProjectSummary(locale, workbook);

      return workbook;
   }
}
