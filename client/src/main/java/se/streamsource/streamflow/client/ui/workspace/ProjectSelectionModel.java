package se.streamsource.streamflow.client.ui.workspace;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

public class ProjectSelectionModel extends AbstractListModel implements
		ComboBoxModel
{
	List<ListItemValue> items;
	
	ListItemValue selectedItem;
	
	public ProjectSelectionModel(ListValue list)
	{
		this.items = list.items().get();
	}

	public Object getElementAt(int index)
	{
		if(items != null && items.size() > 0) 
		{
			return items.get(index);
		}
		return null;
	}

	public int getSize()
	{
		if(items != null)
		{
			return items.size();
		}
		return 0;
	}

	public Object getSelectedItem()
	{
		return selectedItem;
	}

	public void setSelectedItem(Object anItem)
	{
		this.selectedItem = (ListItemValue)anItem;
//		int index = items.indexOf(anItem);
//		fireContentsChanged(this, index, index);
	}
}
