/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

public class PdfDocument
{
   private PDDocument pdf = null;
   private PDPageContentStream contentStream = null;

   private float y = -1;
   private ArrayList<LineObject> pageLines = new ArrayList<>();
   private float maxStringLength;
   private PDRectangle pageSize;

   static float DEFAULT_HEADER_MARGIN = 70;
   static float DEFAULT_FOOTER_MARGIN = 50;
   static float DEFAULT_LEFT_MARGIN = 50;
   static float DEFAULT_RIGHT_MARGIN = 50;

   private float headerMargin = DEFAULT_HEADER_MARGIN;
   private float footerMargin = DEFAULT_FOOTER_MARGIN;
   private float leftMargin = DEFAULT_LEFT_MARGIN;
   private float rightMargin = DEFAULT_RIGHT_MARGIN;

   public PdfDocument()
   {
      this( PDPage.PAGE_SIZE_A4, DEFAULT_HEADER_MARGIN, DEFAULT_FOOTER_MARGIN, DEFAULT_LEFT_MARGIN, DEFAULT_RIGHT_MARGIN );
   }

   public PdfDocument( PDRectangle pageSize, float headerMargin, float footerMargin )
   {
      this.pageSize = pageSize;
      this.headerMargin = headerMargin;
      this.footerMargin = footerMargin;

   }

   public PdfDocument( PDRectangle pageSize, float headerMargin, float footerMargin, float leftMargin, float rightMargin )
   {
      this.pageSize = pageSize;
      this.headerMargin = headerMargin;
      this.footerMargin = footerMargin + 30;
      this.leftMargin = leftMargin;
      this.rightMargin = rightMargin;
   }

   public void init()
   {
      try
      {
         pdf = new PDDocument();

         PDPage page = new PDPage();

         pdf.addPage( page );
         page.setMediaBox( pageSize );
         maxStringLength = page.getMediaBox().getWidth() - ( leftMargin + rightMargin );


         contentStream = new PDPageContentStream( pdf, page );
         contentStream.beginText();

         y = page.getMediaBox().getHeight() - headerMargin;
         contentStream.moveTextPositionByAmount( leftMargin, y );

      } catch (IOException e)
      {
         try
         {
            if (contentStream != null)
            {
               contentStream.close();
            }
         } catch (IOException ioe)
         {
            contentStream = null;
         }
         throw new IllegalStateException( e.getMessage() );
      }
   }

   public PdfDocument println( String text, PdfFont font ) throws IOException
   {
      return print( text + '\n', font );
   }

   public PdfDocument printAlignedRight( String text, PdfFont font ) throws IOException
   {
      pageBreakIfNeeded( font );

      float stringLength = (font.font.getStringWidth( text ) / 1000) * font.size;
      float startFrom = maxStringLength - stringLength;
      contentStream.moveTextPositionByAmount( startFrom, 0 );
      contentStream.setFont( font.font, font.size );
      contentStream.drawString( text );
      contentStream.moveTextPositionByAmount( -startFrom, -font.height );
      y -= font.height;
      return this;
   }

   public PdfDocument print( String text, PdfFont font) throws IOException
   {
      List<String> lines = createLinesFromText( text, font );
      for (String line : lines)
      {
         pageBreakIfNeeded( font );

         contentStream.setFont( font.font, font.size );

         if (contentStream == null)
         {
            throw new IOException( "Error:Expected non-null content stream." );
         }
         contentStream.moveTextPositionByAmount( 0, -font.height );
         y -= font.height;
         contentStream.drawString( line );
      }
      return this;
   }

   public PdfDocument moveUpOneRow(PdfFont font) throws IOException{
      contentStream.moveTextPositionByAmount( 0, font.height );
      return this;
   }


   public PdfDocument insertImage(BufferedImage image) throws IOException
   {
//      contentStream.endText();
      PDJpeg pdfImage = new PDJpeg(pdf, image);
      contentStream.drawXObject(pdfImage, leftMargin, y, 100, 100);
//      contentStream.beginText();

      contentStream.moveTextPositionByAmount(0, -110);
      y -= 100+10;

      return this;
   }

   private void pageBreakIfNeeded( PdfFont font )
         throws IOException
   {
      if (y - font.height < footerMargin )
      {
         PDPage newPage = new PDPage();
         newPage.setMediaBox( pageSize );
         if (contentStream != null)
         {
            contentStream.endText();
            //Drawing all lines here after complete filling page
            if (this.pageLines.size() > 0) {
               for (ListIterator<LineObject> itr = pageLines.listIterator(); itr.hasNext(); ) {
                  LineObject lineObject = itr.next();
                  this.line(lineObject.getEndX(), lineObject.getyPosition(), lineObject.getColor());
                  itr.remove();
               }
            }
            contentStream.close();
         }
         pdf.addPage( newPage );
         contentStream = new PDPageContentStream( pdf, newPage );
         y = newPage.getMediaBox().getHeight() - headerMargin - font.height;
         contentStream.beginText();
         contentStream.moveTextPositionByAmount( leftMargin, y );
      }
   }

   public PdfDocument printLabelAndIndentedText( String label, PdfFont labelFont, String text, PdfFont font , float indentation)
         throws IOException
   {
      pageBreakIfNeeded( labelFont );

      contentStream.moveTextPositionByAmount( 0, 0 );
      contentStream.setFont( labelFont.font, labelFont.size );
      float oldMaxStringLenght = maxStringLength;
      maxStringLength = maxStringLength - 20;
      print( label, labelFont );
      contentStream.moveTextPositionByAmount( indentation, 0 );
      print( text, font );
      contentStream.moveTextPositionByAmount( -indentation, -font.height );
      maxStringLength = oldMaxStringLenght;
      y -=font.height;

      return this;
   }

   public PdfDocument printLabelAndTextWithTabStop( String label, PdfFont labelFont, String text, PdfFont font, float tabStop )
         throws IOException
   {
      pageBreakIfNeeded( labelFont );

      contentStream.moveTextPositionByAmount( 0, 0 );
      contentStream.setFont( labelFont.font, labelFont.size );
      contentStream.drawString( label );
      contentStream.moveTextPositionByAmount( tabStop, font.height );
      float oldMaxStringLength = maxStringLength;
      maxStringLength = maxStringLength - tabStop;
      print( text, font );
      maxStringLength = oldMaxStringLength;
      contentStream.moveTextPositionByAmount( -tabStop, -font.height );

      return this;
   }

   private List<String> createLinesFromText( String text, PdfFont font) throws IOException
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
                        (font.font.getStringWidth( lineWithNextWord ) / 1000) * font.size;
               }
            }
            while (wordIndex < words.length &&
                  lengthIfUsingNextWord <  maxStringLength );
            resultLines.add( nextLineToDraw.toString() );
         }
         lineIndex++;
      }
      return resultLines;
   }

   public PdfDocument underLine( String string, PdfFont font ) throws IOException
   {
      pageLines.add(new LineObject(y, (font.font.getStringWidth(string) / 1000) * font.size, Color.BLACK));
      return this;
   }

   public PdfDocument line() throws IOException
   {
      return line(Color.BLACK);
   }

   public PdfDocument line(Color color) throws IOException
   {
      //Saving line y coordinate to draw before creating new page
      pageLines.add(new LineObject(y - 4, maxStringLength, color));
      //Leaving space for line
      contentStream.moveTextPositionByAmount(0, -8);
      y -= 8;
      return this;
   }

   public PDDocument generateHeaderAndPageNumbers( PdfFont font, String... headers )
   {
      try
      {
         int pageTotal = pdf.getNumberOfPages();
         int pageCount = 1;
         float stringWidth = 0.0f;
         float positionX = 0.0f;

         for (Object o : pdf.getDocumentCatalog().getAllPages())
         {
            String numbering = "" + pageCount + " (" + pageTotal + ")";

            PDPage page = (PDPage) o;
            PDRectangle pageSize = page.findMediaBox();
            float positionY = pageSize.getHeight() - headerMargin + font.height;
            PDPageContentStream stream = new PDPageContentStream( pdf, page, true, true );
            stream.beginText();

            stream.setFont( font.font, font.size );
            stream.moveTextPositionByAmount( 0, positionY );

            for( String header : headers )
            {
               stringWidth = font.font.getStringWidth( header );
               positionX = (pageSize.getWidth() - rightMargin - (stringWidth*font.size)/1000f);

               stream.moveTextPositionByAmount( positionX, 0 );
               stream.drawString( header );
               stream.moveTextPositionByAmount( -positionX, -font.height );
               positionY -= font.height;
            }

            stringWidth = font.font.getStringWidth( numbering );
            positionX = (pageSize.getWidth() - rightMargin - (stringWidth*font.size)/1000f);
            stream.moveTo( pageSize.getLowerLeftX(), pageSize.getLowerLeftY() );
            stream.moveTextPositionByAmount( positionX, 30 - positionY );
            stream.drawString( numbering );

            stream.endText();
            stream.close();
            pageCount++;
         }
      } catch (IOException ioe)
      {
         close();
      }
      return closeAndReturn();
   }

   private PdfDocument line(float endX, float yPos, Color color) throws IOException {
      changeColor(color);
      contentStream.drawLine(leftMargin, yPos, endX + leftMargin, yPos);
      changeColor(Color.BLACK);
      return this;
   }

   public float calculateTabStop( PdfFont font, String... strings )
         throws IOException
   {
      float tabStop = 0.0f;
      for (int i = 0; i < strings.length; i++)
      {
         String string = strings[i];
         float length = (font.font.getStringWidth( string ) / 1000) * font.size;
         tabStop = tabStop < length ? length : tabStop;
      }
      return tabStop + 20;
   }

   public PdfDocument changeColor( Color color )
         throws IOException
   {
      contentStream.setStrokingColor( color );
      contentStream.setNonStrokingColor( color );

      return this;
   }

   public PDDocument closeAndReturn()
   {
      close();
      return pdf;
   }

   private void close()
   {
      if (contentStream != null) {
         try {
            contentStream.endText();
            //Draw lines if document only one page
            if (pdf.getNumberOfPages() < 2) {
               for (ListIterator<LineObject> itr = pageLines.listIterator(); itr.hasNext(); ) {
                  LineObject lineObject = itr.next();
                  this.line(lineObject.getEndX(), lineObject.getyPosition(), lineObject.getColor());
                  itr.remove();
               }
            }
         } catch (IOException e) {
         } finally {
            try {
               contentStream.close();
            } catch (IOException e) {
               contentStream = null;
            }
         }
      }
   }
}
