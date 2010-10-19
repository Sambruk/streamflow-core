/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.infrastructure.ui;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Refresh a Refreshable when a component becomes visible.
 */
public class RefreshWhenVisible
      implements AncestorListener
{
   private Refreshable refreshable;
   private JComponent component;
   private boolean refreshed = false;

   public RefreshWhenVisible( JComponent component, Refreshable refreshable )
   {
      this.refreshable = refreshable;
      this.component = component;

      component.addAncestorListener( this );
   }

   public void ancestorAdded( AncestorEvent event )
   {
      if (refreshable != null && component.isDisplayable() && !refreshed)
      {
         refresh();
      }
   }

   public void ancestorRemoved( AncestorEvent event )
   {
      refreshed = false;
   }

   public void ancestorMoved( AncestorEvent event )
   {
   }

   private void refresh()
   {
      refreshed = true;
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            refreshable.refresh();
         }
      });
   }
}
