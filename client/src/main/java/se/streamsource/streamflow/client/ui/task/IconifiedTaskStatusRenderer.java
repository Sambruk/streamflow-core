package se.streamsource.streamflow.client.ui.task;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.domain.task.TaskStates;

public class IconifiedTaskStatusRenderer extends DefaultTableCellRenderer
{
	public IconifiedTaskStatusRenderer()
	{
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{

		JLabel renderedComponent = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		renderedComponent.setHorizontalAlignment(SwingConstants.CENTER);
		setText(null);
		if (((TaskStates) value).equals(TaskStates.ACTIVE))
		{
			setIcon(i18n.icon(TaskResources.task_status_active_icon,
					i18n.ICON_16));
			setName(i18n.text(TaskResources.task_status_active_text));
			setToolTipText(i18n.text(TaskResources.task_status_active_text));
		} else if (((TaskStates) value).equals(TaskStates.ARCHIVED))
		{
			setIcon(i18n.icon(TaskResources.task_status_archived_icon,
					i18n.ICON_16));
			setName(i18n.text(TaskResources.task_status_archived_text));
			setToolTipText(i18n.text(TaskResources.task_status_archived_text));
		} else if (((TaskStates) value).equals(TaskStates.COMPLETED))
		{
			setIcon(i18n.icon(TaskResources.task_status_completed_icon,
					i18n.ICON_16));
			setName(i18n.text(TaskResources.task_status_completed_text));
			setToolTipText(i18n.text(TaskResources.task_status_completed_text));
		} else if (((TaskStates) value).equals(TaskStates.DONE))
		{
			setIcon(i18n.icon(TaskResources.task_status_done_icon,
					i18n.ICON_16));
			setName(i18n.text(TaskResources.task_status_done_text));
			setToolTipText(i18n.text(TaskResources.task_status_done_text));
		} else if (((TaskStates) value).equals(TaskStates.DROPPED))
		{
			setIcon(i18n.icon(TaskResources.task_status_dropped_icon,
					i18n.ICON_16));
			setName(i18n.text(TaskResources.task_status_dropped_text));
			setToolTipText(i18n.text(TaskResources.task_status_dropped_text));
		} 

		return this;
	}

}
