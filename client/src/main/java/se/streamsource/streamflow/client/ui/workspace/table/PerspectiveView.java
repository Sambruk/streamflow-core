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

package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.util.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

public class PerspectiveView extends JPanel implements Observer
{

   private static final long serialVersionUID = -149885124005347187L;

   @Service
   DialogService dialogs;
   
   @Uses
   Iterable<NameDialog> nameDialogs;

   private CasesTableModel model;

   private JDialog popup;
   private JTextField searchField;
   private JPanel optionsPanel;
   private ApplicationContext context;
   private ObjectBuilderFactory obf;
   private JPanel filterPanel;
   private JPanel viewPanel;
   private JList groupByList;
   private JList sortByList;
   private JList statusList;

   private enum FilterActions
   {
      filterClear,
      filterStatus,
      filterCaseType,
      filterLabel,
      filterAssignee,
      filterProject,
      filterCreatedBy,
      filterCreatedOn,
      filterDueOn,
      viewSorting,
      viewGrouping
   }

   public void initView(final @Service ApplicationContext context, final @Structure ObjectBuilderFactory obf,
         final @Uses CasesTableModel model, @Optional @Uses JTextField searchField)
   {
      this.context = context;

      this.obf = obf;
      this.model = model;
      model.addObserver( this );
      this.searchField = searchField;
      setActionMap( context.getActionMap( this ) );
      
      setFocusable(true);
      setLayout(new BorderLayout());

      filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      javax.swing.Action filterClearAction = getActionMap().get( FilterActions.filterClear.name() );
      JButton filterClearButton = new JButton( filterClearAction );
      filterPanel.add(filterClearButton);

      addPopupButton( filterPanel, FilterActions.filterCreatedOn.name() );
      List<LinkValue> linkValues = model.possibleFilterLinks();

      if( Iterables.matchesAny( Links.withRel( "possibleprojects" ), linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterProject.name() );

      if( Iterables.matchesAny( Links.withRel( "possibleassignees" ), linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterAssignee.name() );

      addPopupButton(filterPanel, FilterActions.filterCaseType.name() );
      addPopupButton(filterPanel, FilterActions.filterLabel.name() );

      if( Iterables.matchesAny( Links.withRel( "possiblecreatedby" ), linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterCreatedBy.name() );

      addPopupButton(filterPanel, FilterActions.filterDueOn.name() );

      if( Iterables.matchesAny( Links.withRel( "possiblestatus" ) , linkValues ) )
         addPopupButton(filterPanel, FilterActions.filterStatus.name() );

      add( filterPanel, BorderLayout.WEST );

      statusList = new StatusList();

      viewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      addPopupButton(viewPanel, FilterActions.viewSorting.name() );
      addPopupButton(viewPanel, FilterActions.viewGrouping.name() );
      add(viewPanel, BorderLayout.EAST);
      
      sortByList = new SortByList();
      groupByList = new GroupByList();

      addHierarchyListener( new HierarchyListener()
      {
         public void hierarchyChanged( HierarchyEvent e )
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0)
            {
               if (!PerspectiveView.this.isShowing())
               {
                  for (Component component : Iterables.flatten( Iterables.iterable( filterPanel.getComponents() ), Iterables.iterable( viewPanel.getComponents() ) ))
                  {
                     if( !(component instanceof JToggleButton) )
                        continue;
                     
                     ((JToggleButton) component).setSelected( false );
                  }
               } 
            }
         }
      } );
   }

   private void addPopupButton(JPanel panel, String action)
   {
      javax.swing.Action filterAction = getActionMap().get(action);
      JToggleButton button = new JToggleButton(filterAction);
      button.addItemListener( new ItemListener()
      {
         public void itemStateChanged(ItemEvent itemEvent)
         {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED) 
            {

               for (Component component : Iterables.flatten(Iterables.iterable(filterPanel.getComponents()), Iterables.iterable(viewPanel.getComponents())))
               {
                  if( !(component instanceof JToggleButton) )
                     continue;
                  if (component != itemEvent.getSource() )
                  {
                     ((JToggleButton)component).setSelected(false);
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
      model.clearFilter();
      if( searchField != null )
         searchField.setText( "" );
      killPopup();
   }

   @Action
   public void filterStatus()
   {
      JPanel statusPanel = new JPanel( new BorderLayout() );
      statusPanel.setPreferredSize( new Dimension( 100, 60 )  );
      statusPanel.setMaximumSize( new Dimension( 100, 60 ) );
      statusPanel.setMaximumSize( new Dimension( 100, 60 ) );
      statusPanel.add( statusList, BorderLayout.CENTER );
      optionsPanel.add( statusPanel );
   }
   
   @Action
   public void filterCaseType()
   {
      SortedList<LinkValue> sortedCaseTypes = new SortedList<LinkValue>( model.getPossibleCaseTypes(),
            new SelectedLinkValueComparator(model.getSelectedCaseTypes()));
      
      PerspectiveOptionsView panel = new PerspectiveOptionsView(context, sortedCaseTypes, model.getSelectedCaseTypeIds(), false, obf );
      optionsPanel.add( panel );
   }

   @Action
   public void filterLabel()
   {
      SortedList<LinkValue> sortedLabels = new SortedList<LinkValue>( model.getPossibleLabels(),
            new SelectedLinkValueComparator( model.getSelectedLabels() ) );
      
      PerspectiveOptionsView panel = new PerspectiveOptionsView( context, sortedLabels, model.getSelectedLabelIds(), false, obf );
      optionsPanel.add(panel);
   }

   @Action
   public void filterAssignee()
   {
      SortedList<LinkValue> sortedAssignees = new SortedList<LinkValue>( model.getPossibleAssignees(),
            new SelectedLinkValueComparator( model.getSelectedAssignees() ) );

      PerspectiveOptionsView panel = new PerspectiveOptionsView( context, sortedAssignees, model.getSelectedAssigneeIds(), false, obf );
      optionsPanel.add(panel);
   }

   @Action
   public void filterProject()
   {
      SortedList<LinkValue> sortedProjects = new SortedList<LinkValue>( model.getPossibleProjects(),
            new SelectedLinkValueComparator( model.getSelectedProjects() ) );

      PerspectiveOptionsView panel = new PerspectiveOptionsView( context, sortedProjects, model.getSelectedProjectIds(), true, obf );
      optionsPanel.add( panel );
   }
   
   @Action
   public void filterCreatedOn(ActionEvent event)
   {
      PerspectivePeriodView period = obf.newObjectBuilder( PerspectivePeriodView.class ).use( model.getCreatedOnModel() ).newInstance();
      optionsPanel.add(period);
   }

   @Action
   public void filterDueOn(ActionEvent event)
   {
      PerspectivePeriodView period = obf.newObjectBuilder( PerspectivePeriodView.class ).use( model.getDueOnModel() ).newInstance();
      optionsPanel.add(period);
   }

   @Action
   public void filterCreatedBy()
   {
      SortedList<LinkValue> sortedCreatedBy = new SortedList<LinkValue>( model.getPossibleCreatedBy(),
            new SelectedLinkValueComparator( model.getSelectedCreatedBy() ) );
      
      PerspectiveOptionsView panel = new PerspectiveOptionsView( context, sortedCreatedBy, model.getSelectedCreatedByIds(), false, obf );
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
      SwingUtilities.invokeLater( new Runnable()
      {

         public void run()
         {
            // Make it impossible to have several popups open at the same time
            if( popup != null )
            {
               popup.dispose();
               popup = null;
            }
            final JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass( JFrame.class, PerspectiveView.this );
            popup = new JDialog( frame );
            popup.getRootPane().registerKeyboardAction( new ActionListener()
            {
               public void actionPerformed( ActionEvent e )
               {
                  killPopup();
                  cleanToggleButtonSelection();
               }
            }, KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), JComponent.WHEN_IN_FOCUSED_WINDOW);
            popup.setUndecorated( true );
            popup.setModal( false );
            popup.setLayout( new BorderLayout() );

            popup.add( optionsPanel, BorderLayout.CENTER );
            Point location = button.getLocationOnScreen();
            popup.setBounds( (int) location.getX(), (int) location.getY() + button.getHeight(), optionsPanel.getWidth(),
                  optionsPanel.getHeight() );
            popup.pack();
            popup.setVisible( true );
            frame.addComponentListener( new ComponentAdapter()
            {
               @Override
               public void componentMoved( ComponentEvent e )
               {
                  if (popup != null)
                  {
                     killPopup();
                     frame.removeComponentListener( this );
                  }
               }
            } );
         }
      } );
   }

   public void killPopup()
   {
      if (popup != null)
      {
         popup.setVisible(false);
         popup.dispose();
         popup = null;
      }
      model.refresh();
   }


   public JDialog getCurrentPopup()
   {
      return popup;
   }

   public void setCurrentPopup( JDialog dialog )
   {
      popup = dialog;
   }

   public void cleanToggleButtonSelection()
   {
      for (Component component : Iterables.flatten( Iterables.iterable( filterPanel.getComponents() ), Iterables.iterable( viewPanel.getComponents() ) ))
      {
         if (!(component instanceof JToggleButton))
            continue;
         if (((JToggleButton) component).isSelected())
         {
            ((JToggleButton) component).setSelected( false );
         }
      }
   }

   public void update( Observable o, Object arg )
   {
      for( Component comp : Iterables.flatten( Iterables.iterable(filterPanel.getComponents()), Iterables.iterable(viewPanel.getComponents()) ) )
      {
         if( comp instanceof JToggleButton )
         {
            JToggleButton button = (JToggleButton)comp;
            boolean selectedIsEmpty = true;
            switch( FilterActions.valueOf( ((ApplicationAction)button.getAction()).getName()))
            {
               case filterStatus:
                  selectedIsEmpty = model.getSelectedStatuses().isEmpty();
                  break;

               case filterAssignee:
                  selectedIsEmpty = model.getSelectedAssigneeIds().isEmpty();
                  break;

               case filterLabel:
                  selectedIsEmpty = model.getSelectedLabelIds().isEmpty();
                  break;

               case filterProject:
                  selectedIsEmpty = model.getSelectedProjectIds().isEmpty();
                  break;

               case filterCaseType:
                  selectedIsEmpty = model.getSelectedCaseTypeIds().isEmpty();
                  break;

               case filterCreatedBy:
                  selectedIsEmpty = model.getSelectedCreatedByIds().isEmpty();
                  break;

               case filterCreatedOn:
                  selectedIsEmpty = Period.none.equals( model.getCreatedOnModel().getPeriod() );
                  break;

               case filterDueOn:
                  selectedIsEmpty = Period.none.equals( model.getDueOnModel().getPeriod() );
                  break;

               case viewSorting:
                  selectedIsEmpty = SortBy.none.equals( model.getSortBy() );
                  break;

               case viewGrouping:
                  selectedIsEmpty = GroupBy.none.equals( model.getGroupBy() );
                  break;
               
               default:

            }
            button.setIcon( selectedIsEmpty ? icon( Icons.down_no_selection, ICON_16 ) : icon( Icons.down_with_selection, ICON_16 ) );
         }
      }
      SwingUtilities.invokeLater( new Runnable(){
         public void run()
         {
            PerspectiveView.this.invalidate();
         }
      });
   }

   class SelectedLinkValueComparator implements Comparator<LinkValue>
   {
     private List<String> selected;
     public SelectedLinkValueComparator(List<String> selectedValues )
     {
         selected = selectedValues;
     }
      public int compare(LinkValue o1, LinkValue o2)
      {
         int val1 = selected.contains( o1.text().get() ) ? 1:0;
         int val2 = selected.contains( o2.text().get() ) ? 1:0;
         int selectedCompare = val2 - val1;
         if (selectedCompare == 0)
            return o1.text().get().compareToIgnoreCase(o2.text().get());
         else
            return selectedCompare;
      }
   }
   
   class StatusList extends JList 
   {
      public StatusList()
      {
         super(new Object[]
         { OPEN.name(), ON_HOLD.name(), CLOSED.name() });
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
                  setIcon( icon( Icons.check, 12 ));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
               } else
               {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0));
               }
               setText(text( valueOf( value.toString() )));
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
         allValues.addAll( Arrays.asList( SortBy.values() ));
         allValues.addAll(Arrays.asList(SortOrder.values()));
         setListData(allValues.toArray());
         
         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (value.equals(model.getSortBy()) || value.equals(model.getSortOrder()))
               {
                  setIcon( icon( Icons.check, 12 ));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
               } else {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
               }
               setText(text((Enum) value));
               if (index == SortBy.values().length-1)
                  setBorder(BorderFactory.createCompoundBorder(new BottomBorder(Color.LIGHT_GRAY, 1, 3), getBorder()));
               return this;
            }});
         
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
                        model.setSortBy((SortBy)selectedValue);
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
                  setIcon( icon( Icons.check, 12 ));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
               } else {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
               }
               setText(text((GroupBy)value));
               return this;
            }});
         
         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  model.setGroupBy( (GroupBy) getSelectedValue());
               }
            }
         });
      }
   }
}