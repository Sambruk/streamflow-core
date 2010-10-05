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

package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.Form;

import java.io.IOException;


@Mixins(SubmittedFormPdfGenerator.Mixin.class)
public interface SubmittedFormPdfGenerator extends ServiceComposite
{
   PDDocument generatepdf( SubmittedFormValue value ) throws IOException;

   abstract class Mixin
         implements SubmittedFormPdfGenerator
   {

      // http://localhost:8082/streamflow/surface/accesspoints/3215d8df-6e8a-44b6-bea5-8b901433479d-19b/endusers/63d441ab-b9fb-4188-92a6-701fb430f855-0/63d441ab-b9fb-4188-92a6-701fb430f855-4/submittedforms/3215d8df-6e8a-44b6-bea5-8b901433479d-146/

      @Structure
      UnitOfWorkFactory uowFactory;

      public PDDocument generatepdf( SubmittedFormValue value ) throws IOException
      {

         PdfDocument document = new PdfDocument();
         document.init();

         PdfFont h1Font = new PdfFont( PDType1Font.HELVETICA_BOLD, 16 );
         PdfFont h2Font = new PdfFont( PDType1Font.HELVETICA_BOLD, 14 );
         PdfFont valueFont = new PdfFont( PDType1Font.HELVETICA, 12 );
         PdfFont descFont = new PdfFont( PDType1Font.HELVETICA_OBLIQUE, 10 );

         Form form = uowFactory.currentUnitOfWork().get( Form.class, value.form().get().identity() );

         document.println( form.getDescription(), h1Font );
         document.println( value.submissionDate().get().toString(), descFont );

         document.line();


         for (SubmittedFieldValue submittedFieldValue : value.values().get())
         {
            Field field = uowFactory.currentUnitOfWork().get( Field.class, submittedFieldValue.field().get().identity() );

            document.print( field.getDescription() + ":", h2Font );
            document.println( submittedFieldValue.value().get(), valueFont );
            document.println( "", valueFont );
         }

         return document.closeAndReturn();
      }


   }
}
