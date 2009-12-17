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
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.FilteredList;
import se.streamsource.streamflow.client.infrastructure.ui.GroupedFilteredList;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.task.TaskActionsModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * JAVADOC
 */
public class SelectUserOrProjectDialog2
      extends JPanel
{
   Dimension dialogSize = new Dimension( 600, 300 );

   public ListItemValue selected;
   public GroupedFilteredList projectList;
   public FilteredList userList;

   public SelectUserOrProjectDialog2( final @Uses TaskActionsModel taskModel,
                                      @Service ApplicationContext context,
                                      @Structure ObjectBuilderFactory obf )
   {
      super( new GridLayout(1, 2) );

      setName( i18n.text( WorkspaceResources.search_projects_users ) );
      setActionMap( context.getActionMap( this ) );

      EventList projects = taskModel.getPossibleProjects();
      EventList<ListItemValue> users = taskModel.getPossibleUsers();

      projectList = new GroupedFilteredList();
      projectList.setEventList( projects );

      userList = new FilteredList();
      userList.setEventList(users);

      add( new JScrollPane( projectList ));
      add( new JScrollPane( userList ));

      projectList.getList().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            userList.getList().clearSelection();
         }
      });

      userList.getList().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            projectList.getList().clearSelection();
         }
      });
   }

   public EntityReference getSelected()
   {
      return selected == null ? null : selected.entity().get();
   }

   @Action
   public void execute()
   {
      selected = (ListItemValue) projectList.getList().getSelectedValue();
      if (selected == null)
         selected = (ListItemValue) userList.getList().getSelectedValue();

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}