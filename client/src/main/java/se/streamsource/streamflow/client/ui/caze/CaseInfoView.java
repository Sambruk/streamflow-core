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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.caze.CaseValue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class CaseInfoView extends JPanel implements Observer
{
   SimpleDateFormat format = new SimpleDateFormat();

	CaseInfoModel model;

   private JLabel caseId = new JLabel();
   private JLabel caseType = new JLabel();
   private JLabel description = new JLabel();
   private JLabel created = new JLabel();
   private JLabel owner = new JLabel();
   private JLabel assignedTo = new JLabel();

   private CaseStatusTableCellRenderer statusRenderer;
   private JTable fakeTable;
   private JPanel statusPanel = new JPanel();

   public CaseInfoView(@Service ApplicationContext appContext)
	{
      super(new ModifiedFlowLayout(FlowLayout.LEFT));

      Font boldFont = description.getFont().deriveFont( Font.BOLD );
      description.setFont( boldFont );
      caseId.setFont( boldFont );
      caseType.setFont( boldFont );
      created.setFont( boldFont );
      owner.setFont( boldFont );

      setFont( getFont().deriveFont(getFont().getSize()-2 ));

      fakeTable = new JTable();
      statusRenderer = new CaseStatusTableCellRenderer();
      add(statusPanel);
      add(description);
      add( caseId );
      add(new JLabel(i18n.text( WorkspaceResources.casetype_column_header ) + ":"));
      add( caseType );
      add(new JLabel(i18n.text( WorkspaceResources.created_column_header )+":"));
      add(created);
      add(new JLabel(i18n.text( WorkspaceResources.owner )+":"));
      add(owner);
      add(assignedTo);

	}

   public void setModel( CaseInfoModel caseInfoModel )
	{
		if (model != null)
			model.deleteObserver(this);

		model = caseInfoModel;

		caseInfoModel.addObserver(this);

      update(null, null);


	}

	public void update(Observable o, Object arg)
	{
      CaseValue aCase = model.getInfo();

      statusPanel.removeAll();
      JComponent comp = (JComponent) statusRenderer.getTableCellRendererComponent( fakeTable, aCase.status().get(), false, false, 0, 0 );
      comp.setBorder( BorderFactory.createEtchedBorder());
      statusPanel.add( comp );

      description.setText( aCase.text().get());

      caseId.setText( aCase.caseId().get() != null ? "(#"+ aCase.caseId().get()+")" : "" );

      caseType.setText( aCase.caseType().get() != null ? aCase.caseType().get() : "" );


      created.setText(format.format( aCase.creationDate().get())+(aCase.createdBy().get() != null ? "("+ aCase.createdBy().get()+")":""));
      owner.setText( aCase.owner().get() );

      if (aCase.assignedTo().get() == null)
         assignedTo.setText( "" );
      else
         assignedTo.setText( "<html>"+i18n.text(WorkspaceResources.assigned_to_header )+":<b>"+ aCase.assignedTo().get()+"</b></html>");

      this.repaint();
	}
}