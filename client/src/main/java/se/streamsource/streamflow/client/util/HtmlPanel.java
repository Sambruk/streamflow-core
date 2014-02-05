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
package se.streamsource.streamflow.client.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

/**
 * Generic HTML viewer pane. Links that have protocol will be opened in dekstop browser. Others
 * will be considered action names, and will cause that action, which has to be in a showing component somewhere,
 * to be invoked.
 */
public class HtmlPanel
      extends JEditorPane
{
   public HtmlPanel( String html )
   {
      super( "text/html", html );

      Font font = UIManager.getFont( "Label.font" );
      String bodyRule = "body { font-family: " + font.getFamily() + "; " +
            "font-size: " + font.getSize() + "pt; }";
      ((HTMLDocument) getDocument()).getStyleSheet().addRule( bodyRule );

      setOpaque( false );
      setBorder( null );
      setEditable( false );
      setFocusable( false );
      addHyperlinkListener( new HyperlinkListener()
      {
         public void hyperlinkUpdate( HyperlinkEvent e )
         {
            if (e.getEventType().equals( HyperlinkEvent.EventType.ACTIVATED ))
            {
               if (e.getURL() == null)
               {
                  String action = e.getDescription();

                  // Invoke action on a component - locate it and execute
                  for (Window window : Frame.getWindows())
                  {
                     if (invokeAction( window, action ))
                        return;
                  }
               } else
               {
                  // Open in browser
                  try
                  {
                     Desktop.getDesktop().browse( e.getURL().toURI() );
                  } catch (IOException e1)
                  {
                     e1.printStackTrace();
                  } catch (URISyntaxException e1)
                  {
                     e1.printStackTrace();
                  }
               }
            }
         }

         private boolean invokeAction( Component component, String name )
         {
            if (!component.isShowing())
               return false;

            if (component instanceof JComponent)
            {
               JComponent jcomp = (JComponent) component;
               ActionMap am = jcomp.getActionMap();
               Action action = am.get( name );
               if (action != null)
               {
                  action.actionPerformed( new ActionEvent( component, ActionEvent.ACTION_PERFORMED, name ) );
                  return true;
               }
            }

            if (component instanceof Container)
            {
               Container container = (Container) component;
               for (Component childComponent : container.getComponents())
               {
                  if (invokeAction( childComponent, name ))
                     return true;
               }
            }

            return false;
         }

      } );
   }
}
