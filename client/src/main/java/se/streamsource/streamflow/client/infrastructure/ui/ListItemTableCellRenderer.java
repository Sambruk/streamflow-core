package se.streamsource.streamflow.client.infrastructure.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;

public class ListItemTableCellRenderer extends DefaultTableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		ListItemValue itemValue = (ListItemValue)value;

		return super.getTableCellRendererComponent(table, itemValue == null ? "" : itemValue.description().get(), isSelected, hasFocus,
				row, column);
	}
}
