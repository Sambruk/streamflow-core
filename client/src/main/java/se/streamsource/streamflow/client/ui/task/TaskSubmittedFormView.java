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

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.swingx.JXTable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.CardLayout;

import se.streamsource.streamflow.client.infrastructure.ui.ToolTipTableCellRenderer;

/**
 * JAVADOC
 */
public class TaskSubmittedFormView
      extends JPanel
{
   private JXTable fieldValues;
   private CardLayout layout = new CardLayout();

   public TaskSubmittedFormView()
   {
      setLayout( layout );

      JScrollPane scroll = new JScrollPane();

      fieldValues = new JXTable();
      fieldValues.setDefaultRenderer( Object.class, new ToolTipTableCellRenderer() );
      scroll.setViewportView( fieldValues );

      add( new JPanel(), "EMPTY" );
      add( scroll, "FORM" );
   }

   public void setModel( TaskSubmittedFormModel model )
   {
      if (model != null)
      {
         fieldValues.setModel( model );

         layout.show( this, "FORM" );
      } else
      {
         layout.show( this, "EMPTY" );
      }
   }

}