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

package se.streamsource.streamflow.client.ui.workspace.context;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.ui.*;
import se.streamsource.streamflow.client.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
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
   private WorkspaceContextModel contextModel;

   public WorkspaceContextView(final @Service ApplicationContext context,
                               final @Structure ObjectBuilderFactory obf,
                               @Uses final CommandQueryClient client)
   {
      setLayout(new BorderLayout());

      setPreferredSize(new Dimension(250, 500));

      this.contextModel = obf.newObjectBuilder(WorkspaceContextModel.class).use(client).newInstance();

      contextList = new JList();
      Comparator<ContextItem> comparator = new ContextItemGroupComparator();

      JTextField filterField = new JTextField();
      SortedList<ContextItem> sortedIssues = new SortedList<ContextItem>(contextModel.getItems(), new Comparator<ContextItem>()
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
      contextModel.refresh();
   }

   public JList getWorkspaceContextList()
   {
      return contextList;
   }
}
