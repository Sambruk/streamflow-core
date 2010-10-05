package se.streamsource.streamflow.web.application.pdf;

import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;

import java.io.IOException;


public class PdfFont
{
   public PDSimpleFont font;
   public int size;
   public float height;

   public PdfFont( PDSimpleFont font, int fontSize )
   {
      this.font = font;
      this.size = fontSize;
      this.font.setEncoding( new WinAnsiEncoding() );
      try
      {
         this.height = (this.font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * this.size * 1.05f;
      } catch (IOException e)
      {
         throw new InstantiationError( e.getMessage() );
      }
   }
}
