/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.SelectLinksDialog;
import se.streamsource.streamflow.client.ui.workspace.SelectLinkDialog;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * JAVADOC
 */
public class CaseActionsView extends JPanel
{
	@Uses
	protected ObjectBuilder<SelectLinkDialog> projectSelectionDialog;

   @Uses
   private ObjectBuilder<ConfirmationDialog> confirmationDialog;

	@Service
	DialogService dialogs;

	@Service
	StreamFlowApplication controller;

   @Structure
   ObjectBuilderFactory obf;

	private CaseActionsModel model;

	private JPanel actionsPanel = new JPanel();

   public CaseActionsView(@Service ApplicationContext context)
	{
		setLayout(new BorderLayout());
      setBorder( new EmptyBorder( 5, 5, 5, 10 ) );
      actionsPanel.setLayout(new GridLayout(0, 1, 5, 5));
		add(actionsPanel, BorderLayout.NORTH);
		setActionMap(context.getActionMap(this));
		MacOsUIWrapper.convertAccelerators(context.getActionMap(
				CaseActionsView.class, this));
	}

   public void refresh()
	{
		Actions actions = model.actions();

		actionsPanel.removeAll();

		ActionMap am = getActionMap();

		for (String action : actions.actions().get())
		{
			javax.swing.Action action1 = am.get(action);
			if (action1 != null)
			{
				JButton button = new JButton(action1);
				button.registerKeyboardAction(action1, (KeyStroke) action1
						.getValue(javax.swing.Action.ACCELERATOR_KEY),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
				button.setHorizontalAlignment(SwingConstants.LEFT);
				actionsPanel.add(button);
//				NotificationGlassPane.registerButton(button);
			}
		}

		revalidate();
		repaint();
	}

	// Case actions
	@Action
	public void open()
	{
		model.open();
		refresh();
	}

	@Action
	public void assign()
	{
		model.assignToMe();
		refresh();
	}

	@Action
	public void close()
	{
      EventList<TitledLinkValue> resolutions = model.getPossibleResolutions();
      if (resolutions.isEmpty())
      {
   		model.close();
   		refresh();
      } else
      {
         SelectLinkDialog dialog = obf.newObjectBuilder( SelectLinkDialog.class )
               .use( resolutions ).newInstance();
         dialogs.showOkCancelHelpDialog(
               WindowUtils.findWindow( this ),
               dialog,
               i18n.text( AdministrationResources.resolve ) );

         if (dialog.getSelected() != null)
         {
            model.resolve(dialog.getSelected());
            refresh();
         }
      }
	}

	@Action
	public void delete()
	{
      ConfirmationDialog dialog = confirmationDialog.newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation) );
      if( dialog.isConfirmed())
      {
		   model.delete();
      }
	}

	@Action
	public void sendto()
	{
		SelectLinkDialog dialog = projectSelectionDialog.use(
				model.getPossibleProjects()).newInstance();
		dialogs.showOkCancelHelpDialog(this, dialog);

		if (dialog.getSelected() != null)
		{
			model.sendTo(dialog.getSelected());
			refresh();
		}
	}

   @Action
   public void onhold()
   {
      model.onHold();
      refresh();
   }

	@Action
	public void reopen()
	{
		model.reopen();
		refresh();
	}

   @Action
   public void resume()
   {
      model.resume();
      refresh();
   }

	@Action
	public void unassign()
	{
		model.unassign();
		refresh();
	}

	public void setModel( CaseActionsModel caseActionsModel )
	{
		this.model = caseActionsModel;
	}
}
