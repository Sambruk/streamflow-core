/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.casesettings;

import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

/**
 * View representation what form that has to be submitted before a case of a particular case type can be closed.
 */
public class FormOnCloseView
   extends JPanel
   implements Observer,TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private FormOnCloseModel model;
   private final ApplicationContext context;

   private StreamflowButton formButton;
   private RemovableLabel selectedForm = new RemovableLabel();

   public FormOnCloseView( @Service ApplicationContext context, @Uses FormOnCloseModel model )
   {
      this.context = context;
      this.model = model;
      model.addObserver( this );
      setActionMap( context.getActionMap( this ) );

      setLayout( new FlowLayout(FlowLayout.LEFT) );

      // Select form
      javax.swing.Action formAction = getActionMap().get( "form" );
      formButton = new StreamflowButton( formAction );

      formButton.registerKeyboardAction( formAction, (KeyStroke) formAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      formButton.setHorizontalAlignment( SwingConstants.LEFT );

       selectedForm.getButton().addActionListener( getActionMap().get( "remove" ) );

      add( formButton );

      add( selectedForm );


      new RefreshWhenShowing( this, model );
   }

   public void update( Observable o, Object arg )
   {
      selectedForm.setRemoveLink( model.getIndex() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }

   @Action
   public void form()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleForms() ).newInstance();

      dialogs.showOkCancelHelpDialog( formButton, dialog, i18n.text( AdministrationResources.choose_form_on_close) );

      new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeFormOnClose( dialog.getSelectedLink() );
            }
         }
      }.execute();
   }

   @Action
   public void remove()
   {
      new CommandTask()
      {

         @Override
         protected void command() throws Exception
         {
            model.changeFormOnClose( selectedForm.getRemoveLink() );
         }
      }.execute();
   }
}
