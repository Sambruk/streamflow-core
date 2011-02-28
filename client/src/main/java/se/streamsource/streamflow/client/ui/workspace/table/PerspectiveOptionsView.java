package se.streamsource.streamflow.client.ui.workspace.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
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
import se.streamsource.streamflow.client.util.FilteredList;
import se.streamsource.streamflow.client.util.i18n;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class PerspectiveOptionsView extends JPanel
{
   private JList itemList;
   private JTextField filterField;
   private final List<String> selectedValues;

   public PerspectiveOptionsView(final @Service ApplicationContext context, @Uses EventList<LinkValue> values,
         @Uses final List<String> selectedValues, @Structure ObjectBuilderFactory obf)
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