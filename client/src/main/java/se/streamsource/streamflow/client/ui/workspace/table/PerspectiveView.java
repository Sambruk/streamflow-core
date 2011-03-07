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

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.CLOSED;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.ON_HOLD;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.OPEN;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.Iterables;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.BottomBorder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.util.Strings;
import ca.odell.glazedlists.SortedList;

public class PerspectiveView extends JPanel
{

   private static final long serialVersionUID = -149885124005347187L;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   private JDialog popup;

   private PerspectiveModel model;
   private JTextField searchField;

   private JPanel optionsPanel;

   private ObjectBuilderFactory obf;

   private JPanel filterPanel;

   private JPanel viewPanel;

   private JList groupByList;

   private JList sortByList;

   private JList statusList;
   private javax.swing.Action savePerspective;

   private DateList createdOnList;

   public void initView(final @Service ApplicationContext context, final @Structure ObjectBuilderFactory obf,
         final @Uses PerspectiveModel model, @Optional @Uses JTextField searchField)
   {

      this.obf = obf;
      this.model = model;
      this.searchField = searchField;
      setActionMap(context.getActionMap(this));

      setFocusable(true);
      setLayout(new BorderLayout());

      // Proxy menu item actions manually
      ApplicationAction savePerspectiveAction = (ApplicationAction) getActionMap().get("savePerspective");
      savePerspective = context.getActionMap().get("savePerspective");
      savePerspective.putValue("proxy", savePerspectiveAction);

      filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      javax.swing.Action filterClearAction = getActionMap().get("filterClear");
      JToggleButton filterClearButton = new JToggleButton(filterClearAction);
      filterPanel.add(filterClearButton);
      addPopupButton(filterPanel, "filterCreatedOn");
      addPopupButton(filterPanel, "filterProject");
      addPopupButton(filterPanel, "filterAssignee");
      addPopupButton(filterPanel, "filterCaseType");
      addPopupButton(filterPanel, "filterLabel");
      addPopupButton(filterPanel, "filterCreatedBy");
      addPopupButton(filterPanel, "filterStatus");

      add(filterPanel, BorderLayout.WEST);

      statusList = new StatusList();
      createdOnList = new DateList();

      viewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      addPopupButton(viewPanel, "viewSorting");
      addPopupButton(viewPanel, "viewGrouping");
      add(viewPanel, BorderLayout.EAST);

      sortByList = new SortByList();
      groupByList = new GroupByList();

      addHierarchyListener(new HierarchyListener()
      {
         public void hierarchyChanged(HierarchyEvent e)
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0)
            {
               if (!PerspectiveView.this.isShowing())
               {
                  for (Component component : Iterables.flatten(Iterables.iterable(filterPanel.getComponents()),
                        Iterables.iterable(viewPanel.getComponents())))
                  {
                     ((JToggleButton) component).setSelected(false);
                  }
                  savePerspective.setEnabled(false);
               } else
               {
                  savePerspective.setEnabled(true);
               }
            }
         }
      });
      new RefreshWhenShowing(this, model);
   }

   private void addPopupButton(JPanel panel, String action)
   {
      javax.swing.Action filterLabel = getActionMap().get(action);
      JToggleButton button = new JToggleButton(filterLabel);
      button.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent itemEvent)
         {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED)
            {

               for (Component component : Iterables.flatten(Iterables.iterable(filterPanel.getComponents()),
                     Iterables.iterable(viewPanel.getComponents())))
               {
                  if (component != itemEvent.getSource())
                  {
                     ((JToggleButton) component).setSelected(false);
                  }
               }
               optionsPanel = new JPanel();
               JToggleButton button = (JToggleButton) itemEvent.getSource();
               showPopup(button);
            } else if (state == ItemEvent.DESELECTED)
            {
               killPopup();
            }
         }
      });
      panel.add(button);
   }

   @Action
   public void filterClear()
   {
      killPopup();
      model.clearFilter();
   }

   @Action
   public void filterStatus()
   {
      optionsPanel.add(statusList);
   }

   @Action
   public void filterCaseType()
   {
      SortedList<LinkValue> sortedCaseTypes = new SortedList<LinkValue>(model.getPossibleCaseTypes(),
            new SelectedLinkValueComparator()
            {
               @Override
               List<String> getSelectedValues()
               {
                  return model.getSelectedCaseTypes();
               }
            });

      PerspectiveOptionsView panel = obf.newObjectBuilder(PerspectiveOptionsView.class)
            .use(sortedCaseTypes, model.getSelectedCaseTypes()).newInstance();
      optionsPanel.add(panel);
   }

   @Action
   public void filterLabel()
   {
      SortedList<LinkValue> sortedLabels = new SortedList<LinkValue>(model.getPossibleLabels(),
            new SelectedLinkValueComparator()
            {
               @Override
               List<String> getSelectedValues()
               {
                  return model.getSelectedLabels();
               }
            });

      PerspectiveOptionsView panel = obf.newObjectBuilder(PerspectiveOptionsView.class)
            .use(sortedLabels, model.getSelectedLabels()).newInstance();
      optionsPanel.add(panel);
   }

   @Action
   public void filterAssignee()
   {
      SortedList<LinkValue> sortedAssignees = new SortedList<LinkValue>(model.getPossibleAssignees(),
            new SelectedLinkValueComparator()
            {
               @Override
               List<String> getSelectedValues()
               {
                  return model.getSelectedAssignees();
               }
            });

      PerspectiveOptionsView panel = obf.newObjectBuilder(PerspectiveOptionsView.class)
            .use(sortedAssignees, model.getSelectedAssignees()).newInstance();
      optionsPanel.add(panel);
   }

   @Action
   public void filterProject()
   {
      SortedList<LinkValue> sortedProjects = new SortedList<LinkValue>(model.getPossibleProjects(),
            new SelectedLinkValueComparator()
            {
               @Override
               List<String> getSelectedValues()
               {
                  return model.getSelectedProjects();
               }
            });

      PerspectiveOptionsView panel = obf.newObjectBuilder(PerspectiveOptionsView.class)
            .use(sortedProjects, model.getSelectedProjects()).newInstance();
      optionsPanel.add(panel);
   }

   @Action
   public void filterCreatedOn(ActionEvent event)
   {
      
      optionsPanel.add(createdOnList);
   }

   @Action
   public void filterCreatedBy()
   {
      SortedList<LinkValue> sortedCreatedBy = new SortedList<LinkValue>(model.getPossibleCreatedBy(),
            new SelectedLinkValueComparator()
            {
               @Override
               List<String> getSelectedValues()
               {
                  return model.getSelectedCreatedBy();
               }
            });

      PerspectiveOptionsView panel = obf.newObjectBuilder(PerspectiveOptionsView.class)
            .use(sortedCreatedBy, model.getSelectedCreatedBy()).newInstance();
      optionsPanel.add(panel);
   }

   @Action
   public void viewSorting()
   {
      optionsPanel.add(sortByList);
   }

   @Action
   public void viewGrouping()
   {
      optionsPanel.add(groupByList);
   }

   private void showPopup(final Component button)
   {
      SwingUtilities.invokeLater(new Runnable()
      {

         public void run()
         {
            final JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, PerspectiveView.this);
            popup = new JDialog(frame);
            popup.setUndecorated(true);
            popup.setModal(false);
            popup.setLayout(new BorderLayout());

            popup.add(optionsPanel, BorderLayout.CENTER);
            Point location = button.getLocationOnScreen();
            popup.setBounds((int) location.getX(), (int) location.getY() + button.getHeight(), optionsPanel.getWidth(),
                  optionsPanel.getHeight());
            popup.pack();
            popup.setVisible(true);
            frame.addComponentListener(new ComponentAdapter()
            {
               @Override
               public void componentMoved(ComponentEvent e)
               {
                  if (popup != null)
                  {
                     killPopup();
                     frame.removeComponentListener(this);
                  }
               }
            });
         }
      });
   }

   public void killPopup()
   {
      if (popup != null)
      {
         popup.setVisible(false);
         popup.dispose();
         popup = null;
         // model.notifyObservers();
      }
   }

   public void setModel(PerspectiveModel model)
   {
      this.model = model;
   }

   @Action
   public Task savePerspective()
   {
      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog(this, dialog, text(WorkspaceResources.save_perspective));
      if (!Strings.empty(dialog.name()))
      {
         return new CommandTask()
         {
            @Override
            public void command() throws Exception
            {
               model.savePerspective(dialog.name(), searchField != null ? searchField.getText() : "");
            }
         };
      } else
         return null;
   }

   abstract class SelectedLinkValueComparator implements Comparator<LinkValue>
   {

      abstract List<String> getSelectedValues();

      public int compare(LinkValue o1, LinkValue o2)
      {
         int val1 = getSelectedValues().contains(o1.text().get()) ? 1 : 0;
         int val2 = getSelectedValues().contains(o2.text().get()) ? 1 : 0;
         int selectedCompare = val2 - val1;
         if (selectedCompare == 0)
            return o1.text().get().compareToIgnoreCase(o2.text().get());
         else
            return selectedCompare;
      }
   }

   class DateList extends JList
   {
      public DateList()
      {
         setListData(new String[] {"1 dag", "1 vecka"});

         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer()
         {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
               setText((String) value);
               return this;
            }
         });

         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  
               }
            }
         });
      }
   }
   
   class StatusList extends JList 
   {
      public StatusList()
      {
         super(new Object[]
         { OPEN.name(), ON_HOLD.name(), CLOSED.name() });
         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer()
         {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (model.getSelectedStatuses().contains(value))
               {
                  setIcon(i18n.icon(Icons.check, 12));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
               } else
               {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
               }
               setText(text(WorkspaceResources.valueOf(value.toString())));
               return this;
            }
         });

         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  String selectedValue = (String) statusList.getSelectedValue();
                  if (selectedValue != null)
                  {
                     if (model.getSelectedStatuses().contains(selectedValue))
                     {
                        model.getSelectedStatuses().remove(selectedValue);
                     } else
                     {
                        model.getSelectedStatuses().add(selectedValue);
                     }
                     statusList.clearSelection();
                  }
               }
            }
         });
      }
   }
   
   class SortByList extends JList
   {

      public SortByList()
      {
         List<Enum> allValues = new ArrayList<Enum>();
         allValues.addAll(Arrays.asList(SortBy.values()));
         allValues.addAll(Arrays.asList(SortOrder.values()));
         setListData(allValues.toArray());

         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer()
         {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (value.equals(model.getSortBy()) || value.equals(model.getSortOrder()))
               {
                  setIcon(i18n.icon(Icons.check, 12));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
               } else
               {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
               }
               setText(text((Enum) value));
               if (index == SortBy.values().length - 1)
                  setBorder(BorderFactory.createCompoundBorder(new BottomBorder(Color.LIGHT_GRAY, 1, 3), getBorder()));
               return this;
            }
         });

         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  Enum selectedValue = (Enum) getSelectedValue();
                  if (selectedValue != null)
                  {
                     if (selectedValue instanceof SortBy)
                     {
                        model.setSortBy((SortBy) selectedValue);
                     } else
                     {
                        model.setSortOrder((SortOrder) selectedValue);
                     }
                     clearSelection();
                     repaint();
                  }
               }
            }
         });
      }
   }

   class GroupByList extends JList
   {

      public GroupByList()
      {
         super(GroupBy.values());
         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer()
         {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (value.equals(model.getGroupBy()))
               {
                  setIcon(i18n.icon(Icons.check, 12));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
               } else
               {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
               }
               setText(text((GroupBy) value));
               return this;
            }
         });

         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  model.setGroupBy((GroupBy) getSelectedValue());
               }
            }
         });
      }
   }
}