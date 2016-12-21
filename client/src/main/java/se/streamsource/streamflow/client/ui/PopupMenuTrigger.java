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
package se.streamsource.streamflow.client.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

/**
 * Generic trigger for opening up a popup menu.
 */
public class PopupMenuTrigger extends MouseAdapter
{
   private final JPopupMenu popup;
   private ListSelectionModel selectionModel;

   public PopupMenuTrigger( JPopupMenu popup )
   {
      this.popup = popup;
   }

   public PopupMenuTrigger( JPopupMenu popup, ListSelectionModel selectionModel )
   {
      this( popup );
      this.selectionModel = selectionModel;
   }

   public void mousePressed( MouseEvent e )
   {
      mouseReleased( e );
   }

   public void mouseReleased( MouseEvent e )
   {
      if (e.isPopupTrigger())
      {
         if (selectionModel != null && selectionModel.isSelectionEmpty())
            return;

         showPopup( e );
      }
   }

   protected void showPopup( MouseEvent e )
   {
      popup.show( e.getComponent(),
            e.getX(), e.getY() );
   }
}
