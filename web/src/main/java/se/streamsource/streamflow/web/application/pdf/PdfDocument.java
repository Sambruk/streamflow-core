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

package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfDocument
{
   private PDDocument pdf = null;
   private PDPageContentStream contentStream = null;

   private float y = -1;
   private float maxStringLength;
   private PDRectangle pageSize;
   private float margin;

   static float DEFAULT_MARGIN = 40;

   public PdfDocument()
   {
      this( PDPage.PAGE_SIZE_A4, DEFAULT_MARGIN );
   }

   public PdfDocument( PDRectangle pageSize, float margin )
   {
      this.pageSize = pageSize;
      this.margin = margin;

   }

   public void init()
   {
      try
      {
         pdf = new PDDocument();

         PDPage page = new PDPage();

         pdf.addPage( page );
         page.setMediaBox( pageSize );
         maxStringLength = page.getMediaBox().getWidth() - 2 * margin;


         contentStream = new PDPageContentStream( pdf, page );
         contentStream.beginText();

         y = page.getMediaBox().getHeight() - margin;
         contentStream.moveTextPositionByAmount( margin, y );

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

   public PdfDocument print( String text, PdfFont font ) throws IOException
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

   public PdfDocument insertImage(BufferedImage image) throws IOException
   {
//      contentStream.endText();
      PDJpeg pdfImage = new PDJpeg(pdf, image);
      contentStream.drawXObject(pdfImage, margin, y, 100, 100);
//      contentStream.beginText();

      contentStream.moveTextPositionByAmount(0, -110);
      y -= 100+10;

      return this;
   }

   private void pageBreakIfNeeded( PdfFont font )
         throws IOException
   {
      if (y - font.height < margin)
      {
         PDPage newPage = new PDPage();
         newPage.setMediaBox( pageSize );
         pdf.addPage( newPage );
         if (contentStream != null)
         {
            contentStream.endText();
            contentStream.close();
         }
         contentStream = new PDPageContentStream( pdf, newPage );
         y = newPage.getMediaBox().getHeight() - margin - font.height;
         contentStream.beginText();
         contentStream.moveTextPositionByAmount( margin, y );
      }
   }

   public PdfDocument printLabelAndText( String label, PdfFont labelFont, String text, PdfFont font, float tabStop )
         throws IOException
   {
      pageBreakIfNeeded( labelFont );

      if( y == pageSize.getHeight() - margin )
         y -= labelFont.height;
      
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

   private List<String> createLinesFromText( String text, PdfFont font ) throws IOException
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
                  lengthIfUsingNextWord < maxStringLength);
            resultLines.add( nextLineToDraw.toString() );
         }
         lineIndex++;
      }
      return resultLines;
   }

   public PdfDocument underLine( String string, PdfFont font ) throws IOException
   {
      return line( (font.font.getStringWidth( string ) / 1000) * font.size );
   }

   public PdfDocument line() throws IOException
   {
      return line( maxStringLength );
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
            float positionY = pageSize.getHeight() - margin + font.height;
            PDPageContentStream stream = new PDPageContentStream( pdf, page, true, true );
            stream.beginText();

            stream.setFont( font.font, font.size );
            stream.moveTextPositionByAmount( 0, positionY );

            for( String header : headers )
            {
               stringWidth = font.font.getStringWidth( header );
               positionX = (pageSize.getWidth() - margin - (stringWidth*font.size)/1000f);

               stream.moveTextPositionByAmount( positionX, 0 );
               stream.drawString( header );
               stream.moveTextPositionByAmount( -positionX, -font.height );
               positionY -= font.height;
            }

            stringWidth = font.font.getStringWidth( numbering );
            positionX = (pageSize.getWidth() - margin - (stringWidth*font.size)/1000f);
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

   private PdfDocument line( float endX ) throws IOException
   {
      contentStream.moveTextPositionByAmount( 0, -4 );
      y -= 4;
      contentStream.drawLine( margin, y, endX + margin, y );
      contentStream.moveTextPositionByAmount( 0, -4 );
      y -= 4;
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
      if (contentStream != null)
      {
         try
         {
            contentStream.endText();
         } catch (IOException e)
         {
         } finally
         {
            try
            {
               contentStream.close();
            } catch (IOException e)
            {
               contentStream = null;
            }
         }
      }
   }


}
