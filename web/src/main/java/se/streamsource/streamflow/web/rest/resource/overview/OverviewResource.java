/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.rest.resource.overview;

import org.apache.poi.ss.usermodel.Workbook;
import org.qi4j.api.util.DateFunctions;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.overview.OverviewContext;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * JAVADOC
 */
public class OverviewResource
   extends CommandQueryResource
   implements SubResources
{
   public OverviewResource( )
   {
      super( OverviewContext.class );
   }

   public OutputRepresentation generateexcelprojectsummary() throws IOException
   {
      final Workbook workbook = context(OverviewContext.class).generateexcelprojectsummary();

      OutputRepresentation representation = new OutputRepresentation(MediaType.APPLICATION_EXCEL)
      {
         @Override
         public void write(OutputStream outputStream) throws IOException
         {
            workbook.write(outputStream);
         }
      };

      Form downloadParams = new Form();
      downloadParams.set( Disposition.NAME_FILENAME, "case_overview_"+DateFunctions.toUtcString(new Date())+".xls" );
      representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT, downloadParams));

      return representation;
   }

   public void resource( String segment ) throws ResourceException
   {
      setRole( Project.class, segment );
      subResource( OverviewProjectResource.class );
   }
}
