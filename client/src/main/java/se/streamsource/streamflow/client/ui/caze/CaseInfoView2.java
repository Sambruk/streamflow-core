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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.caze.CaseValue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class CaseInfoView2 extends JPanel implements Observer
{
   SimpleDateFormat format = new SimpleDateFormat();

   CaseInfoModel model;

   private JLabel title = new JLabel();
   private JLabel caseType = new JLabel();
   private JLabel owner = new JLabel();
   private JLabel assignedTo = new JLabel();
   private JLabel createdBy = new JLabel();

   private CaseStatusTableCellRenderer statusRenderer;
   private JTable fakeTable;
   private JPanel statusPanel = new JPanel();

   public CaseInfoView2( @Service ApplicationContext appContext )
   {
      this.setFocusable( false );
      setFont( getFont().deriveFont( getFont().getSize() - 2 ) );

      FormLayout layout = new FormLayout( "25dlu,180dlu,60dlu,60dlu,60dlu,60dlu", "10dlu,15dlu" );
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

      JLabel createdHeader = new JLabel( i18n.text( WorkspaceResources.created_column_header ) );
      createdHeader.setFocusable( false );
      createdHeader.setForeground( Color.GRAY );

      JLabel assignedHeader = new JLabel( i18n.text( WorkspaceResources.assigned_to_header ) );
      assignedHeader.setFocusable( false );
      assignedHeader.setForeground( Color.GRAY );


      fakeTable = new JTable();
      statusRenderer = new CaseStatusTableCellRenderer();

      CellConstraints cc = new CellConstraints();

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

   }

   public void setModel( CaseInfoModel caseInfoModel )
   {
      if (model != null)
         model.deleteObserver( this );

      model = caseInfoModel;

      caseInfoModel.addObserver( this );

      update( null, null );


   }

   public void update( Observable o, Object arg )
   {
      CaseValue aCase = model.getInfo();

      statusPanel.removeAll();
      JComponent comp = (JComponent) statusRenderer.getTableCellRendererComponent( fakeTable, aCase.status().get(), false, false, 0, 0 );
      comp.setBorder( BorderFactory.createEtchedBorder() );
      comp.setPreferredSize( new Dimension( 15, 15 ) );
      statusPanel.add( comp );

      String titleText = (aCase.caseId().get() != null ? "#" + aCase.caseId().get() + " " : "") + aCase.text().get();
      title.setText( titleText );
      title.setToolTipText( titleText );

      String caseTypeText = "";
      if (aCase.caseType().get() != null)
         caseType.setText( caseTypeText = aCase.caseType().get() + (aCase.resolution().get() != null ? "(" + aCase.resolution().get() + ")" : "") );

      caseType.setToolTipText( caseTypeText );

      String ownerText = aCase.owner().get();
      owner.setText( ownerText );
      owner.setToolTipText( ownerText );

      String createdByText = aCase.createdBy().get();
      createdBy.setText( createdByText );
      createdBy.setToolTipText( createdByText );

      String assignedToText = "";
      if (aCase.assignedTo().get() != null)
         assignedTo.setText( assignedToText = aCase.assignedTo().get() );

      assignedTo.setToolTipText( assignedToText );

      this.repaint();
   }
}