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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.application.pdf.SubmittedFormPdfGenerator;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.SelectedTemplate;
import se.streamsource.streamflow.web.domain.structure.form.Form;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Locale;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class SurfaceSubmittedFormContext
{
   @Service
   SubmittedFormPdfGenerator pdfGenerator;

   @Structure
   UnitOfWorkFactory uowFactory;

   public OutputRepresentation generateformaspdf() throws IOException, URISyntaxException
   {
      SubmittedFormValue submittedFormValue = role( SubmittedFormValue.class );

      Locale locale = role( Locale.class );

      SelectedTemplate.Data selectedTemplate = role( SelectedTemplate.Data.class );
      AttachedFile.Data template = (AttachedFile.Data) selectedTemplate.selectedTemplate().get();
      String uri = null;
      if (template != null)
      {
         uri = template.uri().get();
      }
      Form form = uowFactory.currentUnitOfWork().get( Form.class, submittedFormValue.form().get().identity() );

      final PDDocument pdf = pdfGenerator.generatepdf( submittedFormValue, uri, locale );

      OutputRepresentation representation = new OutputRepresentation( MediaType.APPLICATION_PDF )
      {
         @Override
         public void write( OutputStream outputStream ) throws IOException
         {
            COSWriter writer = null;
            try
            {
               writer = new COSWriter( outputStream );
               writer.write( pdf );
            }
            catch (COSVisitorException e)
            {
               // Todo Handle this error more gracefully...
               e.printStackTrace();
            } finally
            {
               if (pdf != null)
               {
                  pdf.close();
               }
               if (writer != null)
               {
                  writer.close();
               }
            }
         }
      };

      Disposition disposition = new Disposition();
      disposition.setFilename( form.getDescription() + ".pdf" );
      disposition.setType( Disposition.TYPE_ATTACHMENT );
      representation.setDisposition( disposition );

      return representation;
   }
}