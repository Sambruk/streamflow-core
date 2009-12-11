/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.task.TaskModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public class SelectUserOrProjectDialog2
        extends JPanel
{
   Dimension dialogSize = new Dimension( 600, 300 );

   public ListItemValue selected;
   public JList projectList;

   public SelectUserOrProjectDialog2( final @Uses TaskModel taskModel,
                                      @Service ApplicationContext context,
                                      @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );

      setName( i18n.text( WorkspaceResources.search_projects_users ) );
      setActionMap( context.getActionMap( this ) );

      EventList<ListItemValue> projects = taskModel.getPossibleProjects();

      projectList = new JList( new EventListModel<ListItemValue>( projects ) );
      projectList.setCellRenderer( new ListItemListCellRenderer() );

      add( new JScrollPane( projectList ), BorderLayout.WEST );
   }

   public EntityReference getSelected()
   {
      return selected == null ? null : selected.entity().get();
   }

   @Action
   public void execute()
   {
      selected = (ListItemValue) projectList.getSelectedValue();

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}