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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.JavaHelp;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.ui.status.StatusBarView;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * Workspace window
 */
public class WorkspaceWindow
      extends FrameView
{
   public CardLayout cardLayout;

   public WorkspaceWindow( @Service Application application,
                           @Service JavaHelp javaHelp,
                           @Uses WorkspaceMenuBar menu,
                           @Uses final WorkspaceView workspaceView,
                           @Uses AccountSelectionView view,
                           @Uses final AccountSelector accountSelector,
                           @Structure final ObjectBuilderFactory obf )
   {
      super( application );

      final JXFrame frame = new JXFrame( i18n.text( WorkspaceResources.window_name ) );
      frame.setLocationByPlatform( true );

      cardLayout = new CardLayout();
      frame.getContentPane().setLayout( cardLayout );
      frame.getContentPane().add( view, "selector" );
      frame.getContentPane().add( workspaceView, "workspace" );
      frame.getRootPane().setOpaque( true );
      setFrame( frame );
      setMenuBar( menu );

      JXStatusBar bar = new StatusBarView( getContext() );
      setStatusBar( bar );

      frame.setPreferredSize( new Dimension( 1000, 700 ) );
      frame.pack();
      // Turn off java help for 1.0 release
      //javaHelp.enableHelp( this.getRootPane(), "workspace" );

      ListSelectionListener workspaceListener = new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               if (accountSelector.isSelectionEmpty())
               {
                  cardLayout.show( frame.getContentPane(), "selector" );
               } else
               {
                  AccountModel accountModel = accountSelector.getSelectedAccount();

                  workspaceView.setModel( accountModel );

                  cardLayout.show( frame.getContentPane(), "workspace" );
               }
            }
         }
      };

      accountSelector.addListSelectionListener( workspaceListener );
   }


}
