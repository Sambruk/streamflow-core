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
import org.qi4j.api.mixin.Mixins;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.application.pdf.SubmittedFormPdfGenerator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormContext.Mixin.class)
public interface SubmittedFormContext
      extends Context
{

   OutputRepresentation generateformaspdf() throws IOException;


   abstract class Mixin
         extends ContextMixin
         implements SubmittedFormContext
   {


      @Service
      SubmittedFormPdfGenerator pdfGenerator;

      public OutputRepresentation generateformaspdf() throws IOException
      {
         SubmittedFormValue submittedFormValue = roleMap.get( SubmittedFormValue.class );

         final PDDocument pdf = pdfGenerator.generatepdf( submittedFormValue );


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

         return representation;
      }

   }
}