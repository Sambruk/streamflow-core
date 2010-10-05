package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

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

   public PdfDocument print( String text, PdfFont font ) throws IOException
   {
      List<String> lines = createLinesFromText( text, font );
      for (String line : lines)
      {
         if (y < margin)
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
            y = newPage.getMediaBox().getHeight() - margin;
            contentStream.beginText();
            contentStream.moveTextPositionByAmount( margin, y );

         }

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

   public PdfDocument line() throws IOException
   {
      contentStream.moveTextPositionByAmount( 0, -4 );
      y -= 4;
      contentStream.drawLine( margin, y, maxStringLength + margin, y );
      contentStream.moveTextPositionByAmount( 0, -4 );
      y -= 4;
      return this;
   }

   public PDDocument closeAndReturn()
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
      return pdf;
   }
}
