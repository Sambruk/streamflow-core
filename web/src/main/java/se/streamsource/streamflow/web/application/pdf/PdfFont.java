/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.pdf;

import java.io.IOException;

import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;


public class PdfFont
{
   public PDSimpleFont font;
   public int size;
   public float height;

   public PdfFont( PDSimpleFont font, int fontSize )
   {
      this.font = font;
      this.size = fontSize;
      //TODO Ensure that there isn't any problems without setting encoding in new version
//      this.font.setEncoding( new WinAnsiEncoding() );
      try
      {
         this.height = (this.font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * this.size * 1.05f;
      } catch (Exception e)
      {
         throw new InstantiationError( e.getMessage() );
      }
   }
}
