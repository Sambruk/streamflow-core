package se.streamsource.streamflow.client.util;

import org.jdesktop.swingx.JXHyperlink;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.util.Strings;

import javax.swing.*;
import java.awt.*;
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
}
