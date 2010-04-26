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

import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.search.Searchable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.status.StatusBarView;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Automatically switch searchable depending on focus
 * on the component. Add this as focus-listener on
 * the component being searched.
 */
public class SearchFocus implements FocusListener
{
   @Uses
   Searchable searchable;
   @Service
   SingleFrameApplication app;

   public void focusGained( FocusEvent e )
   {
      StatusBarView statusBarView = (StatusBarView) ((JXFrame) app.getMainFrame()).getStatusBar();
      statusBarView.getSearchField().setSearchable( searchable );
   }

   public void focusLost( FocusEvent e )
   {
/*
        StatusBarView statusBarView = (StatusBarView) ((JXFrame) app.getMainFrame()).getStatusBar();
        if (statusBarView.getSearchField().)
        statusBarView.getSearchField().setSearchable(null);
*/
   }
}
