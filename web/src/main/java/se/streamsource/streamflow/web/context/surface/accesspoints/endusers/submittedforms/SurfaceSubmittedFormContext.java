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
package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;

import java.util.Locale;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class SurfaceSubmittedFormContext
{
   @Service
   PdfGeneratorService pdfGenerator;

   @Uses Locale locale;

   public PDDocument generateformaspdf() throws Throwable
   {
      return generatePdf();

   }

   private PDDocument generatePdf() throws Throwable
   {
      SubmittedFormValue submittedFormValue = role(SubmittedFormValue.class);

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

      return pdfGenerator.generateSubmittedFormPdf( submittedFormValue, idData, uri, locale );
   }
}