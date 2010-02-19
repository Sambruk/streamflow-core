package se.streamsource.streamflow.client.infrastructure.ui;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * A glasspane that can be used to notify the user (for a specified time)
 * what the x and y coordinates of their JFrame (window) are after they
 * have moved it.
 *
 * <p/>
 * Copyright (C) 2005 by Jon Lipsky
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class NotificationGlassPane extends JPanel implements MouseListener, ActionListener
{
   public static void install()
   {
      Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
      {
         public void eventDispatched( AWTEvent event )
         {
            if (event instanceof MouseEvent)
            {
               MouseEvent mouseEvent = (MouseEvent) event;
               if (event.getID() == MouseEvent.MOUSE_PRESSED && event.getSource() instanceof JButton)
               {
                  JButton button = (JButton) event.getSource();
                  Action action = button.getAction();
                  if (action != null && action.getValue( Action.ACCELERATOR_KEY ) != null)
                  {
                     for (MouseListener mouseListener : button.getMouseListeners())
                     {
                        if (mouseListener instanceof NotificationGlassPane)
                           return;
                     }

                     registerButton( button );
                  }
               }
            }
         }
      }, AWTEvent.MOUSE_EVENT_MASK );
   }


	// ------------------------------------------------------------------------------------------------------------------
	//  Fields
	// ------------------------------------------------------------------------------------------------------------------

	private boolean installed = false;
	private Component previousGlassPane;
	private Timer timer;
	private int delay = 3000;
	private Action action;
	private JButton button;
	private JFrame frame;

	// ------------------------------------------------------------------------------------------------------------------
	//  Constructors and Getter/Setters
	// ------------------------------------------------------------------------------------------------------------------

	public NotificationGlassPane(JButton aButton)
	{
		button = aButton;
		button.addMouseListener(this);
		frame = UIUtils.getActiveJFrame(button);
		setOpaque(false);
	}

	public int getDelay()
	{
		return delay;
	}

	public void setDelay(int aDelay)
	{
		delay = aDelay;
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Implementation of the methods from MouseListener
	// ------------------------------------------------------------------------------------------------------------------

	public void mouseClicked(MouseEvent e)
	{
		action = button.getAction();
		
		repaint();

		if (!installed)
		{
			previousGlassPane = frame.getGlassPane();
			frame.setGlassPane(this);
			setVisible(true);
			installed = true;
		}

		if (timer == null)
		{
			timer = new Timer(delay, this);
		}
		else
		{
			timer.stop();
			timer.setDelay(delay);
		}

		timer.start();
	}

	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e)
	{
		action = button.getAction();
		
		repaint();

		if (!installed)
		{
			previousGlassPane = frame.getGlassPane();
			frame.setGlassPane(this);
			setVisible(true);
			installed = true;
		}

		if (timer == null)
		{
			timer = new Timer(delay, this);
		}
		else
		{
			timer.stop();
			timer.setDelay(delay);
		}

		timer.start();
	}

	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Implementation of the methods from ActionListener
	// ------------------------------------------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		timer.stop();

		installed = false;
		setVisible(false);
		frame.setGlassPane(previousGlassPane);
		previousGlassPane = null;
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Override methods of JPanel
	// ------------------------------------------------------------------------------------------------------------------

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		Dimension size = getSize();

		Font font = g.getFont();
		int arc = 0;

		int h = size.height;
		int w = size.width;

		if (size.width > 300)
		{
			font = font.deriveFont(Font.PLAIN,48);
			arc = 20;
		}
		else if (size.width > 150)
		{
			font = font.deriveFont(Font.PLAIN,24);
			arc = 10;
		}
		else
		{
			font = font.deriveFont(Font.PLAIN,12);
			arc = 3;
		}

		KeyStroke stroke = ((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
		String text = stroke.toString();
		text += " (" + button.getText() + ")";
		text = text.replaceAll(" pressed", "");

		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D stringBounds = metrics.getStringBounds(text,g);

		int preferredWidth = (int)stringBounds.getWidth()+metrics.getHeight();
		int preferredHeight = (int)stringBounds.getHeight()+metrics.getHeight();

		w = Math.min(preferredWidth,w);
		h = Math.min(preferredHeight,h);

		int x = (size.width - w) / 2;
		int y = (size.height - h) / 7;

		Color vColor = new Color(0, 0, 0, 150);
		g.setColor(vColor);
		g.fillRoundRect(x, y, w, h, arc, arc);

		g.setColor(Color.WHITE);
		x = (size.width - (int)stringBounds.getWidth()) / 2;
		y = (size.height / 5) + ((metrics.getAscent()- metrics.getDescent()) / 2);

		g.drawString(text,x,y);
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Utility Methods
	// ------------------------------------------------------------------------------------------------------------------

	public static void registerButton(JButton aButton)
	{
		new NotificationGlassPane(aButton);
	}
}
