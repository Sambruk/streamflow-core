package se.streamsource.streamflow.client.ui.task;

import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.interaction.gtd.States;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class TaskStatusTableCellRenderer extends DefaultTableCellRenderer
{
   public TaskStatusTableCellRenderer()
   {
   }

   public Component getTableCellRendererComponent( JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column )
   {

      JLabel renderedComponent = (JLabel) super.getTableCellRendererComponent( table, value, isSelected, hasFocus,
            row, column );
      renderedComponent.setHorizontalAlignment( SwingConstants.CENTER );
      setText( null );
      if (value.equals( States.ACTIVE ))
      {
         setIcon( i18n.icon( TaskResources.task_status_active_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( TaskResources.task_status_active_text ) );
         setToolTipText( i18n.text( TaskResources.task_status_active_text ) );
      } else if (value.equals( States.COMPLETED ))
      {
         setIcon( i18n.icon( TaskResources.task_status_completed_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( TaskResources.task_status_completed_text ) );
         setToolTipText( i18n.text( TaskResources.task_status_completed_text ) );
      } else if (value.equals( States.DONE ))
      {
         setIcon( i18n.icon( TaskResources.task_status_done_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( TaskResources.task_status_done_text ) );
         setToolTipText( i18n.text( TaskResources.task_status_done_text ) );
      } else if (value.equals( States.DROPPED ))
      {
         setIcon( i18n.icon( TaskResources.task_status_dropped_icon,
               i18n.ICON_16 ) );
         setName( i18n.text( TaskResources.task_status_dropped_text ) );
         setToolTipText( i18n.text( TaskResources.task_status_dropped_text ) );
      }

      return this;
   }

}
