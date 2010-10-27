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

package se.streamsource.streamflow.client.ui.workspace.cases.info;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.workspace.table.CaseStatusTableCellRenderer;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.resource.caze.CaseValue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;

/**
 * JAVADOC
 */
public class CaseInfoView extends JPanel 
      implements Refreshable,
      TransactionListener
{
   CaseInfoModel model;

   private JLabel title = new JLabel();
   private JLabel caseType = new JLabel();
   private JLabel owner = new JLabel();
   private JLabel assignedTo = new JLabel();
   private JLabel createdBy = new JLabel();

   private CaseStatusTableCellRenderer statusRenderer;
   private JTable fakeTable;
   private JPanel statusPanel = new JPanel();

   public CaseInfoView( @Service ApplicationContext appContext, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      model = obf.newObjectBuilder( CaseInfoModel.class ).use( client ).newInstance();

      this.setFocusable( false );
      setFont( getFont().deriveFont( getFont().getSize() - 2 ) );
      setPreferredSize( new Dimension( 800, 50 ) );

      FormLayout layout = new FormLayout( "25dlu,170dlu,60dlu,60dlu,90dlu,90dlu", "10dlu,15dlu" );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.setBorder( Borders.createEmptyBorder( Sizes.DLUY4,
            Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX2 ) );

      JLabel statusHeader = new JLabel( i18n.text( WorkspaceResources.case_status_header ) );
      statusHeader.setFocusable( false );
      statusHeader.setForeground( Color.GRAY );

      JLabel titleHeader = new JLabel( i18n.text( WorkspaceResources.case_separator ) );
      titleHeader.setFocusable( false );
      titleHeader.setForeground( Color.GRAY );

      JLabel typeHeader = new JLabel( i18n.text( WorkspaceResources.casetype_column_header ) );
      typeHeader.setFocusable( false );
      typeHeader.setForeground( Color.GRAY );

      JLabel ownerHeader = new JLabel( i18n.text( WorkspaceResources.owner ) );
      ownerHeader.setFocusable( false );
      ownerHeader.setForeground( Color.GRAY );

      JLabel createdHeader = new JLabel( i18n.text( WorkspaceResources.created_by ) );
      createdHeader.setFocusable( false );
      createdHeader.setForeground( Color.GRAY );

      JLabel assignedHeader = new JLabel( i18n.text( WorkspaceResources.assigned_to_header ) );
      assignedHeader.setFocusable( false );
      assignedHeader.setForeground( Color.GRAY );


      fakeTable = new JTable();
      statusRenderer = new CaseStatusTableCellRenderer();

      builder.add( statusHeader, new CellConstraints( 1, 1, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets( 0, 5, 0, 0 ) ) );
      builder.add( titleHeader, "2,1,left,bottom" );
      builder.add( typeHeader, "3,1,left,bottom" );
      builder.add( ownerHeader, "4,1,left,bottom" );
      builder.add( createdHeader, "5,1,left,bottom" );
      builder.add( assignedHeader, "6,1,left,bottom" );

      builder.add( statusPanel, "1,2,left,top" );
      builder.add( title, "2,2,left,center" );
      builder.add( caseType, "3,2,left,center" );
      builder.add( owner, "4,2,left,center" );
      builder.add( createdBy, "5,2,left,center" );
      builder.add( assignedTo, "6,2,left,center" );

      new RefreshWhenVisible(this, this);
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (matches( withNames( "changedOwner", "changedCaseType", "changedDescription", "assignedTo", "unassigned", "changedStatus" ), transactions ))
      {
         refresh();
      }
   }

   public void refresh()
   {
      model.refresh();

      CaseValue aCase = model.getInfo();

      statusPanel.removeAll();
      JComponent comp = (JComponent) statusRenderer.getTableCellRendererComponent( fakeTable, aCase.status().get(), false, false, 0, 0 );
      comp.setBorder( BorderFactory.createEtchedBorder() );
      comp.setPreferredSize( new Dimension( 15, 15 ) );
      statusPanel.add( comp );

      String titleText = (aCase.caseId().get() != null ? "#" + aCase.caseId().get() + " " : "") + aCase.text().get();
      title.setText( titleText );
      title.setToolTipText( titleText );

      caseType.setText( aCase.caseType().get() != null ? aCase.caseType().get() + (aCase.resolution().get() != null ? "(" + aCase.resolution().get() + ")" : "") : "" );
      caseType.setToolTipText( caseType.getText() );

      String ownerText = aCase.owner().get();
      owner.setText( ownerText );
      owner.setToolTipText( ownerText );

      String createdByText = aCase.createdBy().get();
      createdBy.setText( createdByText );
      createdBy.setToolTipText( createdByText );


      assignedTo.setText( aCase.assignedTo().get() != null ? aCase.assignedTo().get() : "" );
      assignedTo.setToolTipText( assignedTo.getText() );

      this.repaint();
   }
}