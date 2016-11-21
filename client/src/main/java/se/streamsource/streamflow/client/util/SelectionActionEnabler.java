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

import javax.swing.Action;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Enable actions if a list has a selection
 */
public class SelectionActionEnabler
      implements ListSelectionListener, TreeSelectionListener, ItemListener
{
   private Action[] action;

   public SelectionActionEnabler( Action... action )
   {
      this.action = action;
      for (Action action1 : action)
      {
         action1.setEnabled( false );
      }
   }

   public void valueChanged( ListSelectionEvent e )
   {
      if (!e.getValueIsAdjusting())
      {
         if (((ListSelectionModel) e.getSource()).isSelectionEmpty())
         {
            for (Action action1 : action)
            {
               action1.setEnabled( false );
            }
         } else
         {
            selectionChanged();

            for (Action action1 : action)
            {
               action1.setEnabled(isSelectedValueValid( action1 ) );
            }
         }
      }
   }

   protected void selectionChanged()
   {
      // Overload this method to do whatever is necessary to get additional context
   }

   public void valueChanged( TreeSelectionEvent e )
   {
      if (e.getNewLeadSelectionPath() != null)
      {
         selectionChanged();

         for (Action action1 : action)
         {
            action1.setEnabled(isSelectedValueValid( action1 ) );
         }
      } else
      {
         for (Action action1 : action)
         {
            action1.setEnabled(false);
         }
      }

   }

   /**
    * Override this to add logic for whether the currently selected value is valid or not.
    *
    * @return
    */
   public boolean isSelectedValueValid( Action action )
   {
      return true;
   }

   public void itemStateChanged( ItemEvent e )
   {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
         selectionChanged();

         for (Action action1 : action)
         {
            action1.setEnabled(isSelectedValueValid( action1 ) );
         }
      } else
      {
         for (Action action1 : action)
         {
            action1.setEnabled( false );
         }
      }
   }

   public Action[] getActions()
   {
      return action;
   }
}
