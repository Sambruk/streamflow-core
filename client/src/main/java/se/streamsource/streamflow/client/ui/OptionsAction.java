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

package se.streamsource.streamflow.client.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.util.i18n;

/**
 * Generic trigger for opening up an options menu as a popup.
 */
public class OptionsAction extends AbstractAction
{
   private final JPopupMenu popup;

   public OptionsAction( JPopupMenu popup )
   {
      super( null, i18n.icon( Icons.options, 16 ) );
      this.popup = popup;
   }

   public void actionPerformed( ActionEvent e )
   {
      Component component = (Component) e.getSource();

      popup.show( component,
         0, component.getHeight() );
   }
}