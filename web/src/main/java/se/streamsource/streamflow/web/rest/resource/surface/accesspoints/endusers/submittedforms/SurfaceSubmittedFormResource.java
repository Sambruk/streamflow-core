/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource.surface.accesspoints.endusers.submittedforms;

import static se.streamsource.dci.api.RoleMap.role;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SurfaceSubmittedFormContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;

/**
 * TODO
 */
public class SurfaceSubmittedFormResource
   extends CommandQueryResource
{
   public SurfaceSubmittedFormResource()
   {
      super(SurfaceSubmittedFormContext.class);
   }

   public OutputRepresentation generateformaspdf() throws Throwable
   {
      final PDDocument pdf = context(SurfaceSubmittedFormContext.class).generateformaspdf();

      SubmittedFormValue submittedFormValue = role(SubmittedFormValue.class);

      Form form = module.unitOfWorkFactory().currentUnitOfWork().get(Form.class, submittedFormValue.form().get().identity());

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

      CaseId.Data idData = role( CaseId.Data.class);
      Disposition disposition = new Disposition();
      disposition.setFilename(idData.caseId().get());
      disposition.setType(Disposition.TYPE_ATTACHMENT);
      representation.setDisposition(disposition);

      return representation;
   }
}
