package se.streamsource.streamflow.client.ui.workspace.table;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.CLOSED;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.ON_HOLD;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.OPEN;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;

public class PerspectiveView extends JPanel
{

   private static final long serialVersionUID = -149885124005347187L;

   private JDialog popup;
   
   private PerspectiveModel model;

   private JPanel optionsPanel;

   private ObjectBuilderFactory obf;

   private JPanel filterPanel;

   private JPanel viewPanel;

   private JList groupByList;

   private JList sortByList;

   private JList statusList;

   public void initView(final @Service ApplicationContext context, final @Structure ObjectBuilderFactory obf,
         final @Uses PerspectiveModel model)
   {

      this.obf = obf;
      this.model = model;
      setActionMap(context.getActionMap(this));
      
      setFocusable(true);
      setLayout(new BorderLayout());
      
      filterPanel = new JPanel( new FlowLayout(FlowLayout.LEFT));
      addPopupButton(filterPanel, "filterStatus");
      addPopupButton(filterPanel, "filterCaseType");
      addPopupButton(filterPanel, "filterLabel");
      add(filterPanel, BorderLayout.WEST);

      statusList = new JList(new Object[]{OPEN.name(), ON_HOLD.name(), CLOSED.name()});
      statusList.setSelectedIndex(0);
      statusList.setCellRenderer(new DefaultListCellRenderer(){

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
               setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
            } else {

               setIcon(null);
               setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
            }
            setText(text(WorkspaceResources.valueOf(value.toString())));
            return this;
         }});
      
      statusList.addListSelectionListener(new ListSelectionListener()
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
      
      viewPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT));
      addPopupButton(viewPanel, "viewSorting");
      addPopupButton(viewPanel, "viewGrouping");
      add(viewPanel, BorderLayout.EAST);
      
      sortByList = new SingleOptionEnumPanel<SortBy>(SortBy.values())
      {

         @Override
         public SortBy getModelValue()
         {
            return model.getSortBy();
         }

         @Override
         public void setModelValue(SortBy newValue)
         {
            model.setSortBy(newValue);
         }
      };
      
      groupByList = new SingleOptionEnumPanel<GroupBy>(GroupBy.values())
      {

         @Override
         public GroupBy getModelValue()
         {
            return model.getGroupBy();
         }

         @Override
         public void setModelValue(GroupBy newValue)
         {
            model.setGroupBy(newValue);
         }
      };
      
      addHierarchyListener(new HierarchyListener()
      {
         public void hierarchyChanged(HierarchyEvent e)
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)>0 && !PerspectiveView.this.isShowing())
               for (Component component : Iterables.flatten(Iterables.iterable(filterPanel.getComponents()), Iterables.iterable(viewPanel.getComponents())))
               {
                  ((JToggleButton)component).setSelected(false);
               }
         }
      });
      new RefreshWhenShowing( this, model );
   }

   private void addPopupButton(JPanel panel, String action)
   {
      javax.swing.Action filterLabel = getActionMap().get(action);
      JToggleButton button = new JToggleButton(filterLabel);
      button.addItemListener( new ItemListener()
      {
         public void itemStateChanged(ItemEvent itemEvent) {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED) 
            {
               
               for (Component component : Iterables.flatten(Iterables.iterable(filterPanel.getComponents()), Iterables.iterable(viewPanel.getComponents())))
               {
                  if (component != itemEvent.getSource())
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
   public void filterStatus()
   {
      optionsPanel.add(statusList);
   }
   
   @Action
   public void filterCaseType()
   {
      optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
   }

   @Action
   public void filterLabel()
   {
      PerspectiveOptionsView panel = obf.newObjectBuilder(PerspectiveOptionsView.class).use(model.getPossibleLabels(), model.getSelectedLabels()).newInstance();
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
         model.notifyObservers();
      }
   }
   
   public void setModel(PerspectiveModel model)
   {
      this.model = model;
   }
   
   @Action
   public void managePerspectives()
   {

   }

   @Action
   public void savePerspective()
   {
      model.savePerspective("Nytt perspektiv");
   }
   
   abstract class SingleOptionEnumPanel<T extends Enum<T>> extends JList {
      
      public SingleOptionEnumPanel(Object[] values)
      {
         super(values);
         setSelectedIndex(0);
         setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                  boolean cellHasFocus)
            {
               setFont(list.getFont());
               setBackground(list.getBackground());
               setForeground(list.getForeground());
               if (value.equals(getModelValue()))
               {
                  setIcon(i18n.icon(Icons.check, 12));
                  setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0 ));
               } else {

                  setIcon(null);
                  setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 0 ));
               }
               setText(text((T)value));
               return this;
            }});
         
         addListSelectionListener(new ListSelectionListener()
         {

            public void valueChanged(ListSelectionEvent event)
            {
               if (!event.getValueIsAdjusting())
               {
                  setModelValue( (T) getSelectedValue());
               }
            }
         });
      }

      public abstract T getModelValue();
      public abstract void setModelValue(T newValue);
   }
}