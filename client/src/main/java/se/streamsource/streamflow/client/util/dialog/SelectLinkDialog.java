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

package se.streamsource.streamflow.client.util.dialog;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.util.FilteredList;
import se.streamsource.streamflow.client.util.GroupedFilteredList;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Select one or more links from a list of links. The links may use grouping with the TitledLinkValue subtype.
 */
public class SelectLinkDialog
      extends JPanel
{

   private LinkValue selectedLink;
   private List<LinkValue> selectedLinks = new ArrayList<LinkValue>();
   private JList list;
   private JTextField filterField;

   public SelectLinkDialog( final @Service ApplicationContext context,
                                     @Uses EventList<? extends LinkValue> links,
                                      @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout());

      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      if (Iterables.first( links ) instanceof TitledLinkValue)
      {
         GroupedFilteredList list = new GroupedFilteredList();
         list.getList().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         list.setEventList( (EventList<TitledLinkValue>) links );
         add(list);
         this.list = list.getList();
         this.filterField = list.getFilterField();
      } else
      {
         FilteredList list = new FilteredList();
         list.getList().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
         list.setEventList( (EventList<LinkValue>) links );
         add(list);
         this.list = list.getList();
         this.filterField = list.getFilterField();
      }
   }

   public void setSelectionMode(int selectionMode)
   {
      list.setSelectionMode( selectionMode );
   }

   public JTextField getFilterField()
   {
      return filterField;
   }

   public LinkValue getSelectedLink()
   {
      return selectedLink;
   }

   public List<LinkValue> getSelectedLinks()
   {
      return selectedLinks;
   }

   @Action
   public void execute()
   {
      if (list.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)
      {
         selectedLink = (LinkValue) list.getSelectedValue();
      } else
      {
         for (Object link : list.getSelectedValues())
         {
            selectedLinks.add( (LinkValue) link);
         }
      }

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}