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

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.GroupedFilteredList;
import se.streamsource.dci.value.TitledLinkValue;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class TaskLabelsDialog
      extends JPanel
{
   public GroupedFilteredList filteredList = new GroupedFilteredList();
   public List<LinkValue> selected;

   public TaskLabelsDialog( @Service ApplicationContext context, @Uses EventList<TitledLinkValue> list )
   {
      setActionMap( context.getActionMap( this ) );

      setLayout( new BorderLayout() );

      filteredList.setEventList( list );
      add( filteredList, BorderLayout.CENTER );
   }

   public Iterable<LinkValue> getSelectedLabels()
   {
      return selected;
   }

   @Action
   public void execute()
   {
      selected = new ArrayList<LinkValue>();
      for (Object item : filteredList.getList().getSelectedValues())
      {
         selected.add( (LinkValue) item );
      }

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}