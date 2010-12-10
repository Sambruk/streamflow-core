/*
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

package se.streamsource.streamflow.client.ui.workspace.table;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.util.HtmlPanel;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class CasesView
      extends JPanel
{
   private CasesTableView casesTableView;
   private CasesDetailView detailsView;
   private JSplitPane splitPane;
   private CardLayout cardLayout = new CardLayout();

   public CasesView( @Structure ObjectBuilderFactory obf, @Service ApplicationContext context )
   {
      super();

      setActionMap( context.getActionMap( this ) );

      setLayout( cardLayout );

      JPanel welcomePanel = createWelcomePanel();
      this.detailsView = obf.newObjectBuilder( CasesDetailView.class ).newInstance();

      splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
      splitPane.setOneTouchExpandable( true );
      add( splitPane, BorderLayout.CENTER );

      splitPane.setTopComponent( new JPanel() );
      splitPane.setBottomComponent( detailsView );
      splitPane.setResizeWeight( 0.27D );

      splitPane.setDividerLocation( 1D );
      splitPane.setBorder( BorderFactory.createEmptyBorder() );


      add( welcomePanel, "welcome" );
      add( splitPane, "cases" );

      cardLayout.show( this, "welcome" );
   }

   private JPanel createWelcomePanel()
   {
      JPanel welcomePanel = new JPanel( new BorderLayout() );
      URL logoURL = getClass().getResource( i18n.text( Icons.name_logo ) );
      JEditorPane welcomePane = new HtmlPanel(text( WorkspaceResources.welcome, logoURL.toExternalForm() ) );
      welcomePanel.add( welcomePane, BorderLayout.CENTER );
      return welcomePanel;
   }

   public void showTable( CasesTableView casesTableView )
   {
      cardLayout.show( this, "cases" );
      this.casesTableView = casesTableView;
      splitPane.setTopComponent( casesTableView );
      clearCase();
   }

   public void clearTable()
   {
      cardLayout.show( this, "welcome" );
      casesTableView = null;
      splitPane.setTopComponent( new JPanel() );
      clearCase();
   }

   public void showCase( CommandQueryClient client )
   {
      detailsView.show( client );
   }

   public void clearCase()
   {
      detailsView.clear();
   }

   public CasesTableView getCaseTableView()
   {
      return casesTableView;
   }

   public CaseDetailView getCurrentCaseView()
   {
      return detailsView.getCurrentCaseView();
   }

   public void refresh()
   {
      if (casesTableView != null)
         casesTableView.getModel().refresh();
      detailsView.refresh();
   }
}

