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

package se.streamsource.streamflow.client.ui.administration.templates;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.resource.organization.SelectedTemplatesValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;


public class SelectedTemplatesView extends JPanel
      implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> templateDialog;

   private StateBinder selectedTemplatesBinder;

   JButton defaultTemplateButton;
   JButton formTemplateButton;
   JButton caseTemplateButton;

   RemovableLabel defaultTemplate = new RemovableLabel( RemovableLabel.LEFT );
   RemovableLabel formTemplate = new RemovableLabel( RemovableLabel.LEFT );
   RemovableLabel caseTemplate = new RemovableLabel( RemovableLabel.LEFT );

   private SelectedTemplatesModel model;

   public SelectedTemplatesView( @Service ApplicationContext appContext,
                                @Uses CommandQueryClient client,
                                @Structure ObjectBuilderFactory obf )
   {
      this.model = obf.newObjectBuilder( SelectedTemplatesModel.class ).use( client ).newInstance();
      model.addObserver( this );

      selectedTemplatesBinder = obf.newObject( StateBinder.class );
      selectedTemplatesBinder.addObserver( this );
      selectedTemplatesBinder.addConverter( new StateBinder.Converter()
      {
         public Object toComponent( Object value )
         {
            if (value instanceof LinkValue)
            {
               return ((LinkValue) value).text().get();
            } else
               return value;
         }

         public Object fromComponent( Object value )
         {
            return value;
         }
      } );
      selectedTemplatesBinder.setResourceMap( appContext.getResourceMap( getClass() ) );
      SelectedTemplatesValue template = selectedTemplatesBinder
            .bindingTemplate( SelectedTemplatesValue.class );

      defaultTemplate.setFont( defaultTemplate.getFont().deriveFont(
            Font.BOLD ) );
      defaultTemplate.setPreferredSize( new Dimension( 150, 25) );

      formTemplate.setFont( formTemplate.getFont().deriveFont(
            Font.BOLD ) );
      formTemplate.setPreferredSize( new Dimension( 150, 25) );

      caseTemplate.setFont( caseTemplate.getFont().deriveFont(
            Font.BOLD ) );
      caseTemplate.setPreferredSize( new Dimension( 150, 25) );

      FormLayout layout = new FormLayout( "60dlu, 5dlu, 150:grow", "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref:grow" );

      JPanel panel = new JPanel( layout );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            panel );
      builder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      CellConstraints cc = new CellConstraints();

      setActionMap( appContext.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            SelectedTemplatesView.class, this ) );

      ActionMap am = getActionMap();

      // Select default template
      javax.swing.Action defaultTemplateAction = am.get( "defaultTemplate" );
      defaultTemplateButton = new JButton( defaultTemplateAction );

      defaultTemplateButton.registerKeyboardAction( defaultTemplateAction, (KeyStroke) defaultTemplateAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      defaultTemplateButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( defaultTemplateButton, cc.xy( 1, 3, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( selectedTemplatesBinder.bind( defaultTemplate, template.defaultPdfTemplate() ),
            new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 3, 0, 0, 0 ) ) );

      // Select form template
      javax.swing.Action formTemplateAction = am.get( "formTemplate" );
      formTemplateButton = new JButton( formTemplateAction );

      formTemplateButton.registerKeyboardAction( formTemplateAction, (KeyStroke) formTemplateAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      formTemplateButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( formTemplateButton, cc.xy( 1, 5, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( selectedTemplatesBinder.bind( formTemplate, template.formPdfTemplate() ),
            new CellConstraints( 3, 5, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 3, 0, 0, 0 ) ) );

      // Select case template
      javax.swing.Action caseTemplateAction = am.get( "caseTemplate" );
      caseTemplateButton = new JButton( caseTemplateAction );

      caseTemplateButton.registerKeyboardAction( caseTemplateAction, (KeyStroke) caseTemplateAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      caseTemplateButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( caseTemplateButton, cc.xy( 1, 7, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( selectedTemplatesBinder.bind( caseTemplate, template.casePdfTemplate() ),
            new CellConstraints( 3, 7, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 3, 0, 0, 0 ) ) );

      add( panel, BorderLayout.CENTER );

      selectedTemplatesBinder.updateWith( model.getSelectedTemplatesValue() );

      new RefreshWhenShowing( this, model );
   }


   @Action
   public Task defaultTemplate()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            SelectLinkDialog dialog = templateDialog.use(
                  i18n.text( WorkspaceResources.choose_template ),
                  model.getPossibleTemplates( "possibledefaulttemplates") ).newInstance();

            dialogs.showOkCancelHelpDialog( defaultTemplateButton, dialog );

            if (dialog.getSelectedLink() != null)
            {
               model.setTemplate( dialog.getSelectedLink() );
            }
         }
      };

   }

   @Action
   public Task formTemplate()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            SelectLinkDialog dialog = templateDialog.use(
                  i18n.text( WorkspaceResources.choose_template ),
                  model.getPossibleTemplates( "possibleformtemplates") ).newInstance();

            dialogs.showOkCancelHelpDialog( formTemplateButton, dialog );

            if (dialog.getSelectedLink() != null)
            {
               model.setTemplate( dialog.getSelectedLink() );
            }
         }
      };

   }

   @Action
   public Task caseTemplate()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            SelectLinkDialog dialog = templateDialog.use(
                  i18n.text( WorkspaceResources.choose_template ),
                  model.getPossibleTemplates( "possiblecasetemplates") ).newInstance();

            dialogs.showOkCancelHelpDialog( defaultTemplateButton, dialog );

            if (dialog.getSelectedLink() != null)
            {
               model.setTemplate( dialog.getSelectedLink() );
            }
         }
      };

   }


   public void update( Observable o, Object arg )
   {

      if (o == selectedTemplatesBinder)
      {
         final Property property = (Property) arg;
         if (property.qualifiedName().name().equals( "defaultPdfTemplate" ))
         {
            new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.removeTemplate("setdefaulttemplate");
               }
            }.execute();
         } else if (property.qualifiedName().name().equals( "formPdfTemplate" ))
         {
            new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.removeTemplate("setformtemplate");
               }
            }.execute();
         } else if (property.qualifiedName().name().equals( "casePdfTemplate" ))
         {
            new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.removeTemplate("setcasetemplate");
               }
            }.execute();
         }
      } else
      {
         selectedTemplatesBinder.updateWith( model.getSelectedTemplatesValue() );
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames(
            "defaultPdfTemplateSet", "formPdfTemplateSet", "casePdfTemplateSet"), transactions ))
      {
         model.refresh();
      }
   }
}
