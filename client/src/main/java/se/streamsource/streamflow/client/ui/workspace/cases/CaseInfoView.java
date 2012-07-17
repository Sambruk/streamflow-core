/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PermissionsDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.table.CaseStatusLabel;
import se.streamsource.streamflow.client.util.LinkedLabel;
import se.streamsource.streamflow.client.util.i18n;

/**
 * JAVADOC
 */
public class CaseInfoView extends JPanel 
      implements Observer
{
   CaseModel model;

   private JLabel title = new JLabel("");
   private LinkedLabel caseType;
   private JLabel owner = new JLabel("");
   private JLabel assignedTo = new JLabel("");
   private JLabel createdBy = new JLabel("");
   private JLabel lock = new JLabel( "" );

   private CaseStatusLabel statusLabel = new CaseStatusLabel();

   public CaseInfoView( @Service ApplicationContext appContext, @Uses CaseModel model)
   {
      setActionMap(appContext.getActionMap(this));

      caseType = new LinkedLabel();

      this.model = model;

      this.setFocusable( false );
      setFont( getFont().deriveFont( getFont().getSize() - 2 ) );
      setPreferredSize( new Dimension( 800, 50 ) );

      setLayout( new BorderLayout() );
      lock.setIcon( i18n.icon( CaseResources.case_unrestricted_icon ) );
      add( lock, BorderLayout.WEST );

      JPanel topPanel = new JPanel();
      BoxLayout layout = new BoxLayout(topPanel, BoxLayout.X_AXIS);
      topPanel.setLayout( layout );

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

      addBox(topPanel, statusHeader, statusLabel);
      addBox(topPanel, titleHeader, title);
      addBox(topPanel, typeHeader, caseType);
      addBox(topPanel, ownerHeader, owner);
      addBox(topPanel, createdHeader, createdBy);
      addBox(topPanel, assignedHeader, assignedTo);

      add( topPanel, BorderLayout.CENTER );
      model.addObserver(this);
   }


   private void addBox( JPanel container, JLabel label, JComponent component )
   {
      JPanel box = new JPanel();
      box.setBorder( BorderFactory.createEmptyBorder( 0,0,0,10 ) );
      box.setLayout(new BorderLayout());
      box.add(label, BorderLayout.NORTH);
      box.add(component, BorderLayout.CENTER);
      container.add( box );
   }

   public void update( Observable o, Object arg )
   {
      CaseDTO aCase = model.getIndex();

      if ( aCase.restricted().get() )
      {
         lock.setIcon( i18n.icon( CaseResources.case_restricted_icon ) );
      } else
      {
         lock.setIcon( i18n.icon( CaseResources.case_unrestricted_icon ) );
         lock.setToolTipText( i18n.text( WorkspaceResources.case_is_unrestricted ) );
      }
      PermissionsDTO permissions = model.permissions();
      if ( permissions.readAccess().get() != null &&
           permissions.writeAccess().get() != null )
      {
         lock.setToolTipText( buildToolTipText( permissions ) );
      }

      statusLabel.setStatus( aCase.status().get(), aCase.resolution().get() );

      String titleText = (aCase.caseId().get() != null ? "#" + aCase.caseId().get() + " " : "") + aCase.text().get();
      title.setText( titleText );
      title.setToolTipText( titleText );


      String text = aCase.caseType().get() != null ? aCase.caseType().get().text().get() + (aCase.resolution().get() != null ? "(" + aCase.resolution().get() + ")" : "") : "";
      caseType.setLink(aCase.caseType().get(), text);

      String ownerText = aCase.owner().get();
      owner.setText( ownerText );
      owner.setToolTipText( ownerText );

      String createdByText = aCase.createdBy().get();
      createdBy.setText( createdByText );
      createdBy.setToolTipText( createdByText );


      assignedTo.setText( aCase.assignedTo().get() != null ? aCase.assignedTo().get() : "" );
      assignedTo.setToolTipText( assignedTo.getText() );
   }

   private String buildToolTipText( PermissionsDTO permissions )
   {
      StringBuilder sb = new StringBuilder();
      String read  = permissions.readAccess().get();
      String write = permissions.writeAccess().get();
      sb.append( "<html><b>" )
         .append( i18n.text( WorkspaceResources.restrict ) ).append( "</b><br>" );

      sb.append( read.substring( 0, 1 ).toUpperCase() );
      sb.append( read.substring( 1 ) ).append( "<br>" );
      sb.append( write.substring( 0,1 ).toUpperCase() );
      sb.append( write.substring( 1 ) ).append( "</html>" );
      return sb.toString();
   }
}