package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ModifiedFlowLayout;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LabelsView extends JPanel implements ListDataListener, ActionListener, ListSelectionListener
{
	private LabelsModel model;

    private JPanel labelPanel;

    LabelSelectionView labelSelection;

    public LabelsView(@Uses LabelSelectionView labelSelection)
	{
        this.labelSelection = labelSelection;
        setLayout(new BorderLayout());

        labelPanel = new JPanel(new ModifiedFlowLayout(FlowLayout.LEFT));
        //labelPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        //setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));

        add(labelSelection,BorderLayout.NORTH);
        add(labelPanel, BorderLayout.SOUTH);

        labelSelection.getList().addListSelectionListener( this);
	}

    public void setLabelsModel(LabelsModel model)
    {
        this.model = model;
        model.addListDataListener(this);
        initComponents();
    }

	private void initComponents()
	{
		labelPanel.removeAll();

		for (int i = 0; i < model.getSize(); i++)
		{
			ListItemValue itemValue = model.getElementAt(i);
			LabelView labelView = new LabelView(itemValue);
			labelView.addActionListener(this);
			labelPanel.add(labelView);
		}
        
        labelPanel.revalidate();
        labelPanel.repaint();
        
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

    public void valueChanged( ListSelectionEvent e )
    {
        ListItemValue labelItem = (ListItemValue) labelSelection.getList().getSelectedValue();
        if (labelItem != null)
        {
            labelSelection.getList().clearSelection();
            model.addLabel(labelItem.entity().get());
        }
    }

    public void actionPerformed(ActionEvent e)
	{
		Component component = ((Component) e.getSource());
        LabelView labelView = (LabelView) component.getParent();
        model.removeLabel(labelView.label().entity().get());
	}

    public LabelSelectionView labelSelection()
    {
        return labelSelection;
    }
}
