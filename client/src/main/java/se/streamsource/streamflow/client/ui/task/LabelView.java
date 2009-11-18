package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.swingx.JXLabel;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LabelView extends JPanel
    implements FocusListener, KeyListener
{
	ListItemValue itemValue;
	JButton button;

	public LabelView(ListItemValue itemValue)
	{
		super(new FlowLayout(FlowLayout.LEFT,2,1));
		this.itemValue = itemValue;

        setFocusable(true);
        this.setRequestFocusEnabled(true);
        
		JXLabel label = new JXLabel(itemValue.description().get());
		button = new JButton(i18n.icon(Icons.deleteLabel, 16));
		button.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
        button.setFocusable(false);
        
		this.add(label);
		this.add(button);
        setBorder(BorderFactory.createEtchedBorder());

        addFocusListener(this);
        addKeyListener(this);
        
	}

    public ListItemValue label()
	{
		return itemValue;
	}

	public void addActionListener(ActionListener listener)
	{
		button.addActionListener(listener);
	}

    public void focusGained(FocusEvent e) {
        setBorder(BorderFactory.createEtchedBorder(Color.LIGHT_GRAY ,Color.BLUE));
        repaint();
    }

    public void focusLost(FocusEvent e) {
        setBorder(BorderFactory.createEtchedBorder());
        repaint();
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE
                || e.getKeyChar() == KeyEvent.VK_DELETE)
        {
            button.doClick();
        }
    }

    public void keyReleased(KeyEvent e)
    {
    }
}
