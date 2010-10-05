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

import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
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
import java.util.ArrayList;
import java.util.List;


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

         PDDocument pdf;
         int valueFontSize = 12;
         PDSimpleFont valueFont = PDType1Font.HELVETICA;
         valueFont.setEncoding( new WinAnsiEncoding() );
         float valueFontHeight = valueFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000;
         //calculate font height and increase by 5 percent.
         valueFontHeight = valueFontHeight * valueFontSize * 1.05f;

         int headlineFontSize = 14;
         PDSimpleFont headlineFont = PDType1Font.HELVETICA_BOLD;
         headlineFont.setEncoding( new WinAnsiEncoding() );
         float headlineFontHeight = headlineFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000;
         //calculate font height and increase by 5 percent.
         headlineFontHeight = headlineFontHeight * headlineFontSize * 1.05f;

         int margin = 40;

         pdf = new PDDocument();
         PDPage page = new PDPage();

         pdf.addPage( page );
         page.setMediaBox( PDPage.PAGE_SIZE_A4 );
         PDPageContentStream contentStream = null;
         float y = -1;
         float maxStringLength = page.getMediaBox().getWidth() - 2 * margin;

         contentStream = new PDPageContentStream( pdf, page );
         contentStream.beginText();
         contentStream.setFont( headlineFont, headlineFontSize + 2 );
         y = page.getMediaBox().getHeight() - margin;
         contentStream.moveTextPositionByAmount( margin, y );

         Form form = uowFactory.currentUnitOfWork().get( Form.class, value.form().get().identity() );
         List<String> formDescriptionLines = createLinesFromText( form.getDescription(), headlineFont, headlineFontSize + 2, maxStringLength );

         for (String formDescriptionLine : formDescriptionLines)
         {

            contentStream.moveTextPositionByAmount( 0, -headlineFontHeight + 2 );
            y -= headlineFontHeight + 2;
            contentStream.drawString( formDescriptionLine );
         }

         contentStream.setFont( valueFont, valueFontSize - 1 );
         contentStream.moveTextPositionByAmount( 0, -valueFontHeight - 1 );
         y -= valueFontHeight - 1;
         contentStream.drawString( value.submissionDate().get().toString() );
         contentStream.moveTextPositionByAmount( 0, -4 );
         y -= 4;
         contentStream.drawLine( margin, y, maxStringLength + margin, y );
         contentStream.moveTextPositionByAmount( 0, -4 );
         y -= 4;


         for (SubmittedFieldValue submittedFieldValue : value.values().get())
         {
            Field field = uowFactory.currentUnitOfWork().get( Field.class, submittedFieldValue.field().get().identity() );


            List<String> descriptionLines = createLinesFromText( field.getDescription(), headlineFont, headlineFontSize, maxStringLength );
            for (String descriptionLine : descriptionLines)
            {
               if (y < margin)
               {
                  PDPage newPage = new PDPage();
                  newPage.setMediaBox( PDPage.PAGE_SIZE_A4 );
                  pdf.addPage( newPage );
                  if (contentStream != null)
                  {
                     contentStream.endText();
                     contentStream.close();
                  }
                  contentStream = new PDPageContentStream( pdf, newPage );
                  y = page.getMediaBox().getHeight() - margin;
                  contentStream.beginText();
                  contentStream.moveTextPositionByAmount( margin, y );

               }

               contentStream.setFont( headlineFont, headlineFontSize );

               if (contentStream == null)
               {
                  throw new IOException( "Error:Expected non-null content stream." );
               }
               contentStream.moveTextPositionByAmount( 0, -headlineFontHeight );
               y -= headlineFontHeight;
               contentStream.drawString( descriptionLine + ":" );
            }

            List<String> fieldValueLines = createLinesFromText( submittedFieldValue.value().get(), valueFont, valueFontSize, maxStringLength );
            for (String fieldValueLine : fieldValueLines)
            {
               if (y < margin)
               {
                  PDPage newPage = new PDPage();
                  newPage.setMediaBox( PDPage.PAGE_SIZE_A4 );
                  pdf.addPage( newPage );
                  if (contentStream != null)
                  {
                     contentStream.endText();
                     contentStream.close();
                  }
                  contentStream = new PDPageContentStream( pdf, newPage );
                  y = page.getMediaBox().getHeight() - margin;
                  contentStream.beginText();
                  contentStream.moveTextPositionByAmount( margin, y );

               }

               contentStream.setFont( valueFont, valueFontSize );

               if (contentStream == null)
               {
                  throw new IOException( "Error:Expected non-null content stream." );
               }
               contentStream.moveTextPositionByAmount( 0, -valueFontHeight );
               y -= valueFontHeight;
               contentStream.drawString( fieldValueLine );
            }
            // Add an extra empty line after the field value
            contentStream.moveTextPositionByAmount( 0, -valueFontHeight );
            y -= valueFontHeight;
         }
         if (contentStream != null)
         {
            contentStream.endText();
            contentStream.close();
         }
         return pdf;
      }

      private List<String> createLinesFromText( String text, PDSimpleFont font, float fontSize, float maxStringLength ) throws IOException
      {
         ArrayList<String> resultLines = new ArrayList<String>();
         String[] lines = text.trim().split( "\n" );
         int lineIndex = 0;
         while (lineIndex < lines.length)
         {
            String[] words = lines[lineIndex].trim().split( " " );
            int wordIndex = 0;
            while (wordIndex < words.length)
            {
               StringBuffer nextLineToDraw = new StringBuffer();
               float lengthIfUsingNextWord = 0;
               do
               {
                  nextLineToDraw.append( words[wordIndex] );
                  nextLineToDraw.append( " " );
                  wordIndex++;
                  if (wordIndex < words.length)
                  {
                     String lineWithNextWord = nextLineToDraw.toString() + words[wordIndex];
                     lengthIfUsingNextWord =
                           (font.getStringWidth( lineWithNextWord ) / 1000) * fontSize;
                  }
               }
               while (wordIndex < words.length &&
                     lengthIfUsingNextWord < maxStringLength);
               resultLines.add( nextLineToDraw.toString() );
            }
            lineIndex++;
         }
         return resultLines;
      }
   }
}
