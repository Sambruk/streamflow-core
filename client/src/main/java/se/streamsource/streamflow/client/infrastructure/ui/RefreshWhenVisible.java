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

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.Component;

/**
 * Refresh a Refreshable when a component becomes visible.
 */
public class RefreshWhenVisible
      implements AncestorListener
{
   private Refreshable refreshable;
   private Component component;

   public RefreshWhenVisible( Component component )
   {
      this.component = component;
   }

   public RefreshWhenVisible( Refreshable refreshable, Component component )
   {
      this.refreshable = refreshable;
      this.component = component;
   }

   public void setRefreshable( Refreshable refreshable )
   {
      this.refreshable = refreshable;

      if (refreshable != null && component.isVisible())
         refreshable.refresh();
   }

   public void ancestorAdded( AncestorEvent event )
   {
      if (refreshable != null && event.getAncestor().equals( component ))
      {
         refreshable.refresh();
      }
   }

   public void ancestorRemoved( AncestorEvent event )
   {
   }

   public void ancestorMoved( AncestorEvent event )
   {
   }
}
