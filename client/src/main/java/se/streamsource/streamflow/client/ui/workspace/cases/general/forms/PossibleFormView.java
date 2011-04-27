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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PossibleFormView extends JButton
      implements KeyListener
{
   LinkValue itemValue;

   public PossibleFormView( LinkValue itemValue )
   {
      super(itemValue.text().get(), i18n.icon( Icons.formSubmit, 16 ) ) ;
      this.setHorizontalAlignment( SwingConstants.LEFT );
      this.setToolTipText( itemValue.text().get() );
      this.setFont( this.getFont().deriveFont( (float)this.getFont().getSize()  ));

      this.itemValue = itemValue;

      setFocusable( true );
      this.setRequestFocusEnabled( true );

      addKeyListener( this );
   }

   public LinkValue form()
   {
      return itemValue;
   }

   public void keyTyped( KeyEvent e )
   {
   }

   public void keyPressed( KeyEvent e )
   {
      if (e.getKeyChar() == KeyEvent.VK_SPACE
            || e.getKeyChar() == KeyEvent.VK_ENTER)
      {
         doClick();
      }
   }

   public void keyReleased( KeyEvent e )
   {
   }
}
