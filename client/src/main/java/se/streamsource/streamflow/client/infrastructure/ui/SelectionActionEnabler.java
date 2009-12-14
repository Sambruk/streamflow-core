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

package se.streamsource.streamflow.client.infrastructure.ui;

import javax.swing.Action;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * Enable actions if a list has a selection
 */
public class SelectionActionEnabler
      implements ListSelectionListener, TreeSelectionListener
{
   private Action[] action;

   public SelectionActionEnabler( Action... action )
   {
      this.action = action;
      for (int i = 0; i < action.length; i++)
      {
         Action action1 = action[i];
         action1.setEnabled( false );
      }
   }

   public void valueChanged( ListSelectionEvent e )
   {
      if (!e.getValueIsAdjusting())
      {
         for (int i = 0; i < action.length; i++)
         {
            Action action1 = action[i];
            action1.setEnabled( !((ListSelectionModel) e.getSource()).isSelectionEmpty() && isSelectedValueValid() );
         }
      }
   }

   public void valueChanged( TreeSelectionEvent e )
   {
      for (int i = 0; i < action.length; i++)
      {
         Action action1 = action[i];
         action1.setEnabled( e.getNewLeadSelectionPath() != null );
      }
   }

   /**
    * Override this to add logic for whether the currently selected value is valid or not.
    *
    * @return
    */
   public boolean isSelectedValueValid()
   {
      return true;
   }
}
