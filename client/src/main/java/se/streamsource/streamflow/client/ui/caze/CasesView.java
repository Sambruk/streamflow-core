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
import org.qi4j.api.injection.scope.Uses;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class CasesView
   extends JPanel
{
   private CaseTableView caseTableView;
   private CasesDetailView2 detailsView;

   public CasesView(@Uses CaseTableView caseTableView, @Uses CasesDetailView2 detailsView)
   {
      super(new BorderLayout());
      this.caseTableView = caseTableView;
      this.detailsView = detailsView;
      
      final JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
      splitPane.setOneTouchExpandable( true );
      add( splitPane, BorderLayout.CENTER );

      splitPane.setTopComponent( caseTableView );
      splitPane.setBottomComponent( detailsView );
      splitPane.setResizeWeight( 0.27D );

      splitPane.setDividerLocation( 1D );
      splitPane.setBorder(BorderFactory.createEmptyBorder());
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
      caseTableView.getModel().refresh();
      detailsView.refresh();
   }
}

