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

package se.streamsource.streamflow.client.ui.workspace.cases;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.table.CaseStatusLabel;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.resource.caze.CaseDTO;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class CaseInfoView extends JPanel 
      implements Observer
{
   CaseModel model;

   private JLabel title = new JLabel("");
   private JLabel caseType = new JLabel("");
   private JLabel owner = new JLabel("");
   private JLabel assignedTo = new JLabel("");
   private JLabel createdBy = new JLabel("");

   private CaseStatusLabel statusLabel = new CaseStatusLabel();

   public CaseInfoView( @Service ApplicationContext appContext, @Uses CaseModel model, @Structure ObjectBuilderFactory obf )
   {
      this.model = model;

      this.setFocusable( false );
      setFont( getFont().deriveFont( getFont().getSize() - 2 ) );
      setPreferredSize( new Dimension( 800, 50 ) );

      BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
      setLayout( layout );

      JLabel statusHeader = new JLabel( i18n.text( WorkspaceResources.case_status_header ) );
      statusHeader.setFocusable( false );
      statusHeader.setForeground( Color.GRAY );
      statusLabel.setHorizontalAlignment( JLabel.LEFT );

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

      addBox(statusHeader, statusLabel);
      addBox(titleHeader, title);
      addBox(typeHeader, caseType);
      addBox(ownerHeader, owner);
      addBox(createdHeader, createdBy);
      addBox(assignedHeader, assignedTo);

      model.addObserver( this );
   }

   private void addBox( JLabel label, JComponent component )
   {
      JPanel box = new JPanel();
      box.setBorder( BorderFactory.createEmptyBorder( 0,0,0,10 ) );
      box.setLayout(new BorderLayout());
      box.add(label, BorderLayout.NORTH);
      box.add(component, BorderLayout.CENTER);
      add(box);
   }

   public void update( Observable o, Object arg )
   {
      CaseDTO aCase = model.getIndex();

      statusLabel.setStatus( aCase.status().get(), aCase.resolution().get() );

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
   }
}