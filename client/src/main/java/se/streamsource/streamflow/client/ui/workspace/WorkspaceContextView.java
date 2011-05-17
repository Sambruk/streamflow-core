/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.ContextItemGroupComparator;
import se.streamsource.streamflow.client.ui.ContextItemListRenderer;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.SeparatorContextItemListCellRenderer;
import se.streamsource.streamflow.util.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * JAVADOC
 */
public class WorkspaceContextView
      extends JPanel
      implements Refreshable
{
   private JList contextList;
   private JScrollPane workspaceContextScroll;
   private WorkspaceModel workspaceModel;

   public WorkspaceContextView(final @Service ApplicationContext context,
                               final @Structure ObjectBuilderFactory obf,
                               @Uses final WorkspaceModel workspaceModel)
   {
      this.workspaceModel = workspaceModel;
      setLayout(new BorderLayout());

      setPreferredSize(new Dimension(250, 500));

      contextList = new JList();
      Comparator<ContextItem> comparator = new ContextItemGroupComparator();

      JTextField filterField = new JTextField();
      SortedList<ContextItem> sortedIssues = new SortedList<ContextItem>(workspaceModel.getItems(), new Comparator<ContextItem>()
      {
         public int compare(ContextItem o1, ContextItem o2)
         {
            return o1.getGroup().compareTo(o2.getGroup());
         }
      });
      final FilterList<ContextItem> textFilteredIssues = new FilterList<ContextItem>(sortedIssues, new TextComponentMatcherEditor<ContextItem>(filterField, new TextFilterator<ContextItem>()
      {
         public void getFilterStrings(List<String> strings, ContextItem contextItem)
         {
            if (Strings.empty(contextItem.getGroup()))
               strings.add(contextItem.getName());
            else
               strings.add(contextItem.getGroup());
         }
      }));
      EventList<ContextItem> separatorList = new SeparatorList<ContextItem>(textFilteredIssues, comparator, 1, 10000);
      contextList.setModel(new EventListModel<ContextItem>(separatorList));
      contextList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      contextList.setCellRenderer(new SeparatorContextItemListCellRenderer(new ContextItemListRenderer()));

      workspaceContextScroll = new JScrollPane(contextList);

      add(filterField, BorderLayout.NORTH);
      add(workspaceContextScroll, BorderLayout.CENTER);

      new RefreshWhenShowing(this, this);
   }

   public void refresh()
   {
      workspaceModel.refresh();

      new Task<Void, Void>(Application.getInstance())
      {
         @Override
         protected Void doInBackground() throws Exception
         {
            workspaceModel.refreshCounts();
            return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
      }.execute();
   }

   public JList getWorkspaceContextList()
   {
      return contextList;
   }

   public boolean showContext(CaseModel caseModel)
   {
      boolean result = false;
      // do not switch context on searches
      if (contextList.getSelectedValue() != null &&
            ((ContextItem) contextList.getSelectedValue()).getRelation().equals("search"))
         return result;
      CaseDTO caze = caseModel.getIndex();
      for (ContextItem contextItem : workspaceModel.getItems())
      {
         if (!Strings.empty(caze.assignedTo().get()))
         {
            if (contextItem.getGroup().equals(caze.owner().get()) && contextItem.getRelation().equals("assign"))
            {
               contextList.setSelectedValue(contextItem, true);
               result = true;
               break;
            }
         } else
         {
            if (contextItem.getGroup().equals(caze.owner().get()) && contextItem.getRelation().equals("inbox"))
            {
               contextList.setSelectedValue(contextItem, true);
               result = true;
               break;
            }
         }
      }

      return result;
   }

   public WorkspaceModel getModel()
   {
      return workspaceModel;
   }
}
