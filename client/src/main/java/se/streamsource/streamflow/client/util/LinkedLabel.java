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
package se.streamsource.streamflow.client.util;

import org.jdesktop.swingx.JXHyperlink;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.util.Strings;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Font;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Show a LinkValue. If the href property is set, then
 * show as a clickable getRemoveLink, otherwise as a plain label
 */
public class LinkedLabel
   extends JPanel
{
   CardLayout card = new CardLayout();
   JLabel label = new JLabel();
   JXHyperlink link = new JXHyperlink();
   private LinkValue linkValue;

   public LinkedLabel()
   {
      setLayout(card);

      add(label, "label");
      add(link, "getRemoveLink");

      card.show(this, "label");

      link.setClickedColor(link.getUnclickedColor());
   }

   public void setLink(LinkValue linkValue, String text)
   {
      this.linkValue = linkValue;
      if (linkValue == null)
      {
         label.setText(text);
         card.show(this, "label");
      } else
      {
         if (Strings.empty(linkValue.href().get()))
         {
            label.setText(text);
            card.show(this, "label");
         } else
         {
            try
            {
               link.setURI(new URI(linkValue.href().get()));
               link.setText(text);
               card.show(this, "getRemoveLink");
            } catch (URISyntaxException e)
            {
               label.setText(text);
               card.show(this, "label");
            }
         }
      }
   }

   public LinkValue getLinkValue()
   {
      return linkValue;
   }

   @Override
   public void setFont(Font font)
   {
      super.setFont(font);
      if (label != null)
         label.setFont(font);
      if (link != null)
         link.setFont(font);
   }

   public void setText(String text)
   {
      label.setText(text);
      link.setText(text);
   }

   @Override
   public void setEnabled( boolean enabled )
   {
      label.setEnabled( enabled );
      link.setEnabled( enabled );
   }
}
