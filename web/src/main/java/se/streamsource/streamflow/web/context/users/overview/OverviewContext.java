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

package se.streamsource.streamflow.web.context.users.overview;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.entity.user.OverviewQueries;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * JAVADOC
 */
public class OverviewContext
      implements IndexContext<LinksValue>
{
   public LinksValue index()
   {
      OverviewQueries queries = RoleMap.role( OverviewQueries.class );

      return queries.getProjectsSummary();
   }

   public OutputRepresentation generateexcelprojectsummary() throws IOException
   {
      Locale locale = RoleMap.role( Locale.class );

      final Workbook workbook = new HSSFWorkbook();

      OverviewQueries queries = RoleMap.role( OverviewQueries.class );

      queries.generateExcelProjectSummary( locale, workbook );

      OutputRepresentation representation = new OutputRepresentation( MediaType.APPLICATION_EXCEL )
      {
         @Override
         public void write( OutputStream outputStream ) throws IOException
         {
            workbook.write( outputStream );
         }
      };

      return representation;
   }
}
