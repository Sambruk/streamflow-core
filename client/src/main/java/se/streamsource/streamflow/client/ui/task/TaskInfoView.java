/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.TaskValue;

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
public class TaskInfoView extends JPanel implements Observer
{
   SimpleDateFormat format = new SimpleDateFormat();

	TaskInfoModel model;

   private JLabel taskId = new JLabel();
   private JLabel taskType = new JLabel();
   private JLabel description = new JLabel();
   private JLabel created = new JLabel();
   private JLabel owner = new JLabel();
   private JLabel assignedTo = new JLabel();

   private TaskStatusTableCellRenderer statusRenderer;
   private JTable fakeTable;
   private JPanel statusPanel = new JPanel();

   public TaskInfoView(@Service ApplicationContext appContext)
	{
      super(new ModifiedFlowLayout(FlowLayout.LEFT));

      Font boldFont = description.getFont().deriveFont( Font.BOLD );
      description.setFont( boldFont );
      taskId.setFont( boldFont );
      taskType.setFont( boldFont );
      created.setFont( boldFont );
      owner.setFont( boldFont );

      setFont( getFont().deriveFont(getFont().getSize()-2 ));

      fakeTable = new JTable();
      statusRenderer = new TaskStatusTableCellRenderer();
      add(statusPanel);
      add(description);
      add(taskId);
      add(new JLabel(i18n.text( WorkspaceResources.tasktype_column_header) + ":"));
      add(taskType);
      add(new JLabel(i18n.text( WorkspaceResources.created_column_header )+":"));
      add(created);
      add(new JLabel(i18n.text( WorkspaceResources.owner )+":"));
      add(owner);
      add(assignedTo);

	}

	public void setModel(TaskInfoModel taskInfoModel)
	{
		if (model != null)
			model.deleteObserver(this);

		model = taskInfoModel;

		taskInfoModel.addObserver(this);

      update(null, null);


	}

	public void update(Observable o, Object arg)
	{
      TaskValue task = model.getInfo();

      statusPanel.removeAll();
      JComponent comp = (JComponent) statusRenderer.getTableCellRendererComponent( fakeTable, task.status().get(), false, false, 0, 0 );
      comp.setBorder( BorderFactory.createEtchedBorder());
      statusPanel.add( comp );

      description.setText( task.text().get());

      taskId.setText( task.taskId().get() != null ? "(#"+task.taskId().get()+")" : "" );

      taskType.setText( task.taskType().get() != null ? task.taskType().get() : "" );


      created.setText(format.format( task.creationDate().get())+(task.createdBy().get() != null ? "("+task.createdBy().get()+")":""));
      owner.setText( task.owner().get() );

      if (task.assignedTo().get() == null)
         assignedTo.setText( "" );
      else
         assignedTo.setText( "<html>"+i18n.text(WorkspaceResources.assigned_to_header )+":<b>"+task.assignedTo().get()+"</b></html>");
	}
}