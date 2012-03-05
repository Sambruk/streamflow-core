/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.overview;

import java.awt.Dimension;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.client.ui.account.AccountModel;
import se.streamsource.streamflow.client.ui.account.AccountSelector;
import se.streamsource.streamflow.client.ui.menu.OverviewMenuBar;
import se.streamsource.streamflow.client.util.JavaHelp;
import se.streamsource.streamflow.client.util.i18n;

/**
 * Overview window
 */
public class OverviewWindow
      extends FrameView
{
   public OverviewWindow(
         @Service Application application,
         @Service JavaHelp javaHelp,
         @Uses OverviewMenuBar menu,
         @Structure final Module module,
         @Uses final AccountSelector accountSelector )
   {
      super( application );

      final JXFrame frame = new JXFrame( i18n.text( OverviewResources.window_name ) );
      frame.setLocationByPlatform( true );

      setFrame( frame );
      setMenuBar( menu );

      frame.setPreferredSize( new Dimension( 1300, 800 ) );
      frame.pack();

      accountSelector.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               if (accountSelector.isSelectionEmpty())
               {
                  frame.getContentPane().removeAll();
               } else
               {
                  frame.getContentPane().removeAll();

                  AccountModel selectedAccount = accountSelector.getSelectedAccount();
                  OverviewView overviewView = module.objectBuilderFactory().newObjectBuilder(OverviewView.class).use( selectedAccount.newOverviewModel(), selectedAccount.newWorkspaceModel().newCasesModel()).newInstance();

                  frame.getContentPane().add( overviewView );
               }
            }
         }
      } );
      // Turn off java help for 1.0 release
      //javaHelp.enableHelp( this.getRootPane(), "overview" );
   }

}