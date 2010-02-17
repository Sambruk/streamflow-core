/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.TaskValue;

import javax.swing.*;
import java.awt.*;
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

   private JLabel description = new JLabel();
   private JLabel created = new JLabel();
   private JLabel owner = new JLabel();
   private JLabel assignedTo = new JLabel();

   public TaskInfoView(@Service ApplicationContext appContext)
	{
      super(new FlowLayout(FlowLayout.LEFT));

      description.setFont( description.getFont().deriveFont(Font.BOLD ));
      created.setFont( description.getFont().deriveFont(Font.BOLD ));
      owner.setFont( description.getFont().deriveFont(Font.BOLD ));

      add(description);
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
      description.setText( task.text().get());
      created.setText(format.format( task.creationDate().get()));
      owner.setText( task.owner().get() );

      if (task.assignedTo().get() == null)
         assignedTo.setText( "" );
      else
         assignedTo.setText( "<html>"+i18n.text(WorkspaceResources.assigned_to_header )+":<b>"+task.assignedTo().get()+"</b></html>");
	}
}