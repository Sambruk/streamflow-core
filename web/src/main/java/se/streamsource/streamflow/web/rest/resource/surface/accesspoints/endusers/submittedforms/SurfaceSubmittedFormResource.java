package se.streamsource.streamflow.web.rest.resource.surface.accesspoints.endusers.submittedforms;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SurfaceSubmittedFormContext;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import static se.streamsource.dci.api.RoleMap.role;

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

   public OutputRepresentation generateformaspdf() throws IOException, URISyntaxException
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

      Disposition disposition = new Disposition();
      disposition.setFilename(form.getDescription() + ".pdf");
      disposition.setType(Disposition.TYPE_ATTACHMENT);
      representation.setDisposition(disposition);

      return representation;
   }
}
