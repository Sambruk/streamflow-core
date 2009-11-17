package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.swingx.JXLabel;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class LabelView extends JPanel
{
	ListItemValue itemValue;
	JButton button;

	public LabelView(ListItemValue itemValue)
	{
		super(new FlowLayout(FlowLayout.LEFT,2,1));
		this.itemValue = itemValue;
		JXLabel label = new JXLabel(itemValue.description().get());
		button = new JButton(i18n.icon(Icons.deleteLabel, 16));
		button.setBorder(new EmptyBorder(new Insets(0,0,0,0)));

		this.add(label);
		this.add(button);
        setBorder(BorderFactory.createEtchedBorder());
        
	}

    public ListItemValue label()
	{
		return itemValue;
	}

	public void addActionListener(ActionListener listener)
	{
		button.addActionListener(listener);
	}
}
