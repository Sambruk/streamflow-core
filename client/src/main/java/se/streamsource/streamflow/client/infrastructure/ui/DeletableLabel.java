package se.streamsource.streamflow.client.infrastructure.ui;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXLabel;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;

public class DeletableLabel extends JPanel
{
	ListItemValue itemValue;
	private Icon icon;
	JButton button;

	public DeletableLabel(ListItemValue itemValue)
	{
		super(new FlowLayout());
		this.itemValue = itemValue;
		JXLabel label = new JXLabel(itemValue.description().get());
		button = new JButton("Del", new ImageIcon(
				"/icons/delete-16x16.png"));
		button.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));
		this.add(label);
		this.add(button);
	}

	public ListItemValue getContent()
	{
		return itemValue;
	}

	public void addActionListener(ActionListener listener)
	{
		button.addActionListener(listener);
	}
}
