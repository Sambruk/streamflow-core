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

package se.streamsource.streamflow.client.ui.workspace.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.util.BottomBorder;
import se.streamsource.streamflow.client.util.FilteredList;
import se.streamsource.streamflow.client.util.i18n;
import ca.odell.glazedlists.EventList;

public class PerspectiveOptionsView extends JPanel
{
   private JList itemList;
   private JTextField filterField;
   private final List<String> selectedValues;

   public PerspectiveOptionsView(final @Service ApplicationContext context, @Uses EventList<LinkValue> values,
         @Uses final ArrayList<String> selectedValues, @Structure ObjectBuilderFactory obf)
   {

      super(new BorderLayout());
      this.selectedValues = selectedValues;

      FilteredList list = new FilteredList();
      list.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setEventList((EventList<LinkValue>) values);

      add(list);
      this.itemList = list.getList();
      this.filterField = list.getFilterField();

      itemList.setCellRenderer(new DefaultListCellRenderer(){
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
               boolean cellHasFocus)
         {
            LinkValue linkValue = (LinkValue) value;
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            if (selectedValues.contains(linkValue.text().get()))
            {
               setIcon(i18n.icon(Icons.check, 12));
               setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
            } else {

               setIcon(null);
               setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
            }
            setText(linkValue.text().get());
            if (index == selectedValues.size()-1)
               setBorder(BorderFactory.createCompoundBorder(new BottomBorder(Color.LIGHT_GRAY, 1, 3), getBorder()));
            return this;
         }
      });

      itemList.addListSelectionListener(new ListSelectionListener()
      {

         public void valueChanged(ListSelectionEvent event)
         {
            if (!event.getValueIsAdjusting())
            {
               LinkValue linkValue = (LinkValue) itemList.getSelectedValue();
               if (linkValue != null)
               {
                  if (selectedValues.contains(linkValue.text().get()))
                  {
                     selectedValues.remove(linkValue.text().get());
                  } else
                  {
                     selectedValues.add(linkValue.text().get());
                  }
                  itemList.clearSelection();
               }
            }
         }
      });
      //
      // Skip filtering if short list
      if (values.size() < 10)
         filterField.setVisible(false);
   }

   public void setSelectionMode(int selectionMode)
   {
      itemList.setSelectionMode(selectionMode);
   }

   public JTextField getFilterField()
   {
      return filterField;
   }

}