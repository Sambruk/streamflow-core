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
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;


@Mixins(SubmittedFormPdfGenerator.Mixin.class)
public interface SubmittedFormPdfGenerator extends ServiceComposite
{
   PDDocument generatepdf( SubmittedFormValue value, CaseId.Data id, @Optional String templateFileName, Locale locale ) throws IOException, URISyntaxException;

   abstract class Mixin
         implements SubmittedFormPdfGenerator
   {

      @Structure
      UnitOfWorkFactory uowFactory;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      AttachmentStore store;

      public PDDocument generatepdf( SubmittedFormValue value, CaseId.Data id, String templateUri, Locale locale ) throws IOException, URISyntaxException
      {

         ResourceBundle bundle = ResourceBundle.getBundle(
               SubmittedFormPdfGenerator.class.getName(), locale );
         
         PdfDocument document = new PdfDocument();
         document.init();

         PdfFont h1Font = new PdfFont( PDType1Font.HELVETICA_BOLD, 16 );
         PdfFont h2Font = new PdfFont( PDType1Font.HELVETICA_BOLD, 14 );
         PdfFont valueFont = new PdfFont( PDType1Font.HELVETICA, 12 );
         PdfFont descFont = new PdfFont( PDType1Font.HELVETICA_OBLIQUE, 10 );

         Form form = uowFactory.currentUnitOfWork().get( Form.class, value.form().get().identity() );

         document.println( bundle.getString( "caseid") + ": " + id.caseId().get(), h1Font);
         document.print( bundle.getString( "form") + ": " + form.getDescription(), h2Font );
         document.println( bundle.getString( "submission_date") + ": " + DateFormat.getDateInstance( DateFormat.MEDIUM, locale ).format( value.submissionDate().get() ), descFont );

         document.line();


         for (SubmittedFieldValue submittedFieldValue : value.values().get())
         {
            FieldEntity field = uowFactory.currentUnitOfWork().get( FieldEntity.class, submittedFieldValue.field().get().identity() );

            document.print( field.getDescription() + ":", h2Font );
            if (field.fieldValue().get() instanceof DateFieldValue)
            {
               try
               {

                  Date date = (new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" )).parse( submittedFieldValue.value().get() );
                  document.println( DateFormat.getDateInstance( DateFormat.MEDIUM, Locale.getDefault() ).format( date ), valueFont );
               } catch (ParseException e)
               {
                  document.println( "N/A", valueFont );
               }

            } else if( field.fieldValue().get() instanceof AttachmentFieldValue )
            {
               try
               {
                  AttachmentFieldSubmission attachment = vbf.newValueFromJSON( AttachmentFieldSubmission.class, submittedFieldValue.value().get() );
                  document.println( attachment.name().get(), valueFont );
               } catch ( ConstructionException e )
               {
                  //ignore
               }

            } else
            {
               document.println( submittedFieldValue.value().get(), valueFont );
            }
            document.println( "", valueFont );
         }

         PDDocument submittedFormPdf = document.closeAndReturn();
         submittedFormPdf.getDocumentInformation().setCreator( "Streamflow" );
         Calendar calendar = Calendar.getInstance();
         calendar.setTime( value.submissionDate().get() );
         submittedFormPdf.getDocumentInformation().setCreationDate( calendar );
         submittedFormPdf.getDocumentInformation().setTitle( form.getDescription() );

         if (templateUri != null)
         {

            String attachmentId = new URI( templateUri ).getSchemeSpecificPart();
            InputStream templateStream = store.getAttachment( attachmentId );
            Underlay underlay = new Underlay();
            submittedFormPdf = underlay.underlay( submittedFormPdf, templateStream );
         }
         return submittedFormPdf;
      }


   }
}
