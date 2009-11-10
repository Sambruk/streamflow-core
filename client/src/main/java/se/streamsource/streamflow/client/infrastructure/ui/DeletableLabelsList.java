package se.streamsource.streamflow.client.infrastructure.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;

public class DeletableLabelsList extends JPanel implements ListDataListener,
		ActionListener
{
	private ListValue elements;
	
	public DeletableLabelsList()
	{
	}

	public void setListValue(ListValue list)
	{
		this.elements = list;
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		initComponents();
	}

	private void initComponents()
	{
		this.removeAll();

		for (int i = 0; i < elements.items().get().size(); i++)
		{
			ListItemValue itemValue = elements.items().get().get(i);
			DeletableLabel label = new DeletableLabel(itemValue);
			label.addActionListener(this);
			this.add(label);
		}
	}

	public void contentsChanged(ListDataEvent e)
	{
		initComponents();
	}

	public void intervalAdded(ListDataEvent e)
	{
		initComponents();
	}

	public void intervalRemoved(ListDataEvent e)
	{
		initComponents();
	}

	public void actionPerformed(ActionEvent e)
	{
		Component component = ((Component) e.getSource()).getParent();
		if (component instanceof DeletableLabel)
		{
			DeletableLabel label = (DeletableLabel) component;

			firePropertyChange("labels", null, label.getContent().entity().get());
		}
	}
}
