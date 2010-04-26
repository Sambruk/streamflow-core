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

package se.streamsource.streamflow.client.infrastructure.ui;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;
import java.io.StringReader;

/**
 * This class is able to extract the first h3 heading from a html body tag.
 */
public class HtmlErrorMessageExtractor extends HTMLEditorKit.ParserCallback
{
   boolean body = false;
   boolean grabHeading = false;

   String heading = "";

   public static String parse( String msg )
   {
      HTMLEditorKit.Parser parser = new HtmlParserGetter().getParser();
      HtmlErrorMessageExtractor extractor = new HtmlErrorMessageExtractor();
      try
      {
         parser.parse( new StringReader( msg ), extractor, true );
      } catch (IOException e1)
      {
         // do nothing
      }

      return extractor.getHeading();
   }

   @Override
   public void handleText( char[] data, int pos )
   {
      if (body && grabHeading)
      {
         heading = new String( data );
      }
   }

   @Override
   public void handleStartTag( HTML.Tag t, MutableAttributeSet a, int pos )
   {
      if (t == HTML.Tag.BODY)
      {
         body = true;
      }

      if (body && t == HTML.Tag.H3)
      {
         grabHeading = true;
      }
   }

   @Override
   public void handleEndTag( HTML.Tag t, int pos )
   {
      if (t == HTML.Tag.BODY)
      {
         body = false;
      }

      if (body && t == HTML.Tag.H3)
      {
         grabHeading = false;
      }
   }

   public String getHeading()
   {
      return heading;
   }
}
