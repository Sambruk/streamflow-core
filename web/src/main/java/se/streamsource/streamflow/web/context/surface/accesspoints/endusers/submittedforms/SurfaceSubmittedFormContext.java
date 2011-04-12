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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms;

import org.apache.pdfbox.exceptions.*;
import org.apache.pdfbox.pdfwriter.*;
import org.apache.pdfbox.pdmodel.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.unitofwork.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.web.application.pdf.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.user.*;

import java.io.*;
import java.net.*;
import java.util.*;

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
      SubmittedFormValue submittedFormValue = role(SubmittedFormValue.class);

      Locale locale = role(Locale.class);

      FormPdfTemplate.Data selectedTemplate = role( FormPdfTemplate.Data.class);
      AttachedFile.Data template = (AttachedFile.Data) selectedTemplate.formPdfTemplate().get();

      if (template == null)
      {
         ProxyUser proxyUser = role(ProxyUser.class);
         template = (AttachedFile.Data) ((FormPdfTemplate.Data) proxyUser.organization().get()).formPdfTemplate().get();

         if( template == null)
         {
            template = (AttachedFile.Data) ((DefaultPdfTemplate.Data) proxyUser.organization().get()).defaultPdfTemplate().get();
         }
      }
      String uri = null;
      if (template != null)
      {
         uri = template.uri().get();
      }

      CaseId.Data idData = role( CaseId.Data.class);

      Form form = uowFactory.currentUnitOfWork().get(Form.class, submittedFormValue.form().get().identity());

      final PDDocument pdf = pdfGenerator.generatepdf(submittedFormValue, idData, uri, locale);

      OutputRepresentation representation = new OutputRepresentation(MediaType.APPLICATION_PDF)
      {
         @Override
         public void write(OutputStream outputStream) throws IOException
         {
            COSWriter writer = null;
            try
            {
               writer = new COSWriter(outputStream);
               writer.write(pdf);
            } catch (COSVisitorException e)
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
      disposition.setFilename(form.getDescription() + ".pdf");
      disposition.setType(Disposition.TYPE_ATTACHMENT);
      representation.setDisposition(disposition);

      return representation;
   }
}