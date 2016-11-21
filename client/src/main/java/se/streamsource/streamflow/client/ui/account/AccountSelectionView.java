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
package se.streamsource.streamflow.client.ui.account;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.i18n;

/**
 * JAVADOC
 */
public class AccountSelectionView
      extends JPanel
{
   public AccountSelectionView( @Uses final AccountSelector accountSelector )
   {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      JPanel accountSelection = new JPanel( new BorderLayout() );
      JScrollPane scroll = new JScrollPane( );
      scroll.getViewport().add( accountSelector );
      accountSelection.add( scroll, BorderLayout.CENTER );
      accountSelection.setMaximumSize( new Dimension( 300, 200 ) );
      accountSelection.setPreferredSize( new Dimension( 300, 200 ) );
      accountSelection.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), i18n.text( WorkspaceResources.select_account ) ) );

      Box verticalBox = Box.createVerticalBox();
      verticalBox.add( Box.createVerticalGlue());
      verticalBox.add(accountSelection);
      verticalBox.add(Box.createVerticalGlue());

      add(Box.createHorizontalGlue());
      add(verticalBox);
      add(Box.createHorizontalGlue());
   }
}
