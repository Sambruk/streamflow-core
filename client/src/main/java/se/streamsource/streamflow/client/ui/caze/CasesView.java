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

package se.streamsource.streamflow.client.ui.caze;

import org.apache.poi.ss.usermodel.Workbook;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * JAVADOC
 */
public class CasesView
   extends JPanel
{
   private CaseTableView caseTableView;
   private CasesDetailView2 detailsView;
   private JSplitPane splitPane;
   private CardLayout cardLayout = new CardLayout();

   public CasesView(@Structure ObjectBuilderFactory obf)
   {
      super();

      setLayout( cardLayout );

      JPanel welcomePanel = new JPanel(new BorderLayout());
      welcomePanel.add( new JLabel( text( WorkspaceResources.welcome ), JLabel.CENTER ), BorderLayout.CENTER );
      this.detailsView = obf.newObjectBuilder( CasesDetailView2.class ).newInstance();

      splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
      splitPane.setOneTouchExpandable( true );
      add( splitPane, BorderLayout.CENTER );

      splitPane.setTopComponent( new JPanel() );
      splitPane.setBottomComponent( detailsView );
      splitPane.setResizeWeight( 0.27D );

      splitPane.setDividerLocation( 1D );
      splitPane.setBorder(BorderFactory.createEmptyBorder());


      add(welcomePanel, "welcome");
      add(splitPane, "cases");

      cardLayout.show( this, "welcome" );
   }

   public void showTable(CaseTableView caseTableView)
   {
      cardLayout.show( this, "cases" );
      this.caseTableView = caseTableView;
      splitPane.setTopComponent( caseTableView );
      clearCase();
   }

   public void clearTable()
   {
      cardLayout.show( this, "welcome" );
      caseTableView = null;
      splitPane.setTopComponent( new JPanel() );
      clearCase();
   }

   public void showCase( CommandQueryClient client)
   {
      detailsView.show( client );
   }

   public void clearCase()
   {
      detailsView.clear();
   }

   public CaseTableView getCaseTableView()
   {
      return caseTableView;
   }

   public CaseDetailView getCurrentCaseView()
   {
      return detailsView.getCurrentCaseView();
   }

   public void refresh()
   {
      if (caseTableView != null)
         caseTableView.getModel().refresh();
      detailsView.refresh();
   }
}

