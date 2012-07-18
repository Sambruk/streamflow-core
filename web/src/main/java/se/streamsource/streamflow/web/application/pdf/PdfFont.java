/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
