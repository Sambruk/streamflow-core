/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.infrastructure.ui;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JList with popup menu
 */
public class JListPopup
      extends JList
{
   public JListPopup( ListModel dataModel, final JPopupMenu menu )
   {
      super( dataModel );

      setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      addMouseListener( new MouseAdapter()
      {
         public void mouseClicked( MouseEvent me )
         {
            // if right mouse button clicked (or me.isPopupTrigger())
            if (SwingUtilities.isRightMouseButton( me )
                  && !isSelectionEmpty()
                  && locationToIndex( me.getPoint() )
                  == getSelectedIndex())
            {
               menu.show( JListPopup.this, me.getX(), me.getY() );
            }
         }
      }
      );
   }
}
