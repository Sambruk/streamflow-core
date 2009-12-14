/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui;

import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
