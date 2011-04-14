/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.ui.administration.surface;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.api.administration.surface.AccessPointDTO;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import java.awt.*;
import java.util.*;


public class AccessPointView
      extends JPanel
      implements Observer, TransactionListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> projectDialog;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> caseTypeDialog;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> formDialog;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> templateDialog;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> labelSelectionDialog;

   private CaseLabelsView labels;
   private JLabel selectedCaseType = new JLabel();
   private JButton caseTypeButton;
   private JButton labelButton;
   private JButton projectButton;
   private JLabel selectedProject = new JLabel();
   private JButton formButton;
   private JLabel selectedForm = new JLabel();

   private JButton templateButton;
   private RemovableLabel selectedTemplate = new RemovableLabel();

   private AccessPointModel model;

   private StateBinder accessPointBinder;

   public AccessPointView( @Service ApplicationContext appContext,
                           @Uses CommandQueryClient client,
                           @Structure ObjectBuilderFactory obf )
   {
      this.labels = obf.newObjectBuilder( CaseLabelsView.class ).use( client.getSubClient( "labels" ) ).newInstance();
      this.model = obf.newObjectBuilder( AccessPointModel.class ).use( client, labels.getModel() ).newInstance();
      model.addObserver( this );

      setLayout( new BorderLayout() );

      accessPointBinder = obf.newObject( StateBinder.class );
      accessPointBinder.addObserver( this );
      accessPointBinder.addConverter( new StateBinder.Converter()
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
      accessPointBinder.setResourceMap( appContext.getResourceMap( getClass() ) );
      AccessPointDTO template = accessPointBinder
            .bindingTemplate( AccessPointDTO.class );

      FormLayout layout = new FormLayout( "60dlu, 5dlu, 150:grow", "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, default:grow" );

      JPanel panel = new JPanel( layout );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            panel );
      builder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      CellConstraints cc = new CellConstraints();

      setActionMap( appContext.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            AccessPointView.class, this ) );

      selectedProject.setFont( selectedProject.getFont().deriveFont( Font.BOLD ) );

      selectedCaseType.setFont( selectedCaseType.getFont().deriveFont(
            Font.BOLD ) );

      selectedForm.setFont( selectedForm.getFont().deriveFont(
            Font.BOLD ) );

      selectedTemplate.setFont( selectedTemplate.getFont().deriveFont(
            Font.BOLD ) );

      ActionMap am = getActionMap();

      // Select project
      javax.swing.Action projectAction = am.get( "project" );
      projectButton = new JButton( projectAction );
      projectButton.registerKeyboardAction( projectAction, (KeyStroke) projectAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      projectButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( projectButton, cc.xy( 1, 1 ) );

      builder.add( accessPointBinder.bind( selectedProject, template.project() ),
            new CellConstraints( 3, 1, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets( 5, 0, 0, 0 ) ) );


      // Select case type
      javax.swing.Action caseTypeAction = am.get( "casetype" );
      caseTypeButton = new JButton( caseTypeAction );
      caseTypeButton.registerKeyboardAction( caseTypeAction, (KeyStroke) caseTypeAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      caseTypeButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( caseTypeButton, cc.xy( 1, 3 ) );

      builder.add( accessPointBinder.bind( selectedCaseType, template.caseType() ),
            new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets( 5, 0, 0, 0 ) ) );


      // Select labels
      javax.swing.Action labelAction = labels.getActionMap().get( "addLabel" );
      labelButton = new JButton( labelAction );

      labelButton.registerKeyboardAction( labelAction, (KeyStroke) labelAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      labelButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( labelButton, cc.xy( 1, 5, CellConstraints.FILL, CellConstraints.TOP ) );

      labels.setPreferredSize( new Dimension( 500, 60 ) );
      builder.add( labels,
            new CellConstraints( 3, 5, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );

      // Select form
      javax.swing.Action formAction = am.get( "form" );
      formButton = new JButton( formAction );

      formButton.registerKeyboardAction( formAction, (KeyStroke) formAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      formButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( formButton, cc.xy( 1, 7, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( accessPointBinder.bind( selectedForm, template.form() ),
            new CellConstraints( 3, 7, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );

      // Select template
      javax.swing.Action templateAction = am.get( "template" );
      templateButton = new JButton( templateAction );

      templateButton.registerKeyboardAction( templateAction, (KeyStroke) templateAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      templateButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( templateButton, cc.xy( 1, 9, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( accessPointBinder.bind( selectedTemplate, template.template() ),
            new CellConstraints( 3, 9, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 3, 0, 0, 0 ) ) );

      add( panel, BorderLayout.CENTER );

      accessPointBinder.updateWith( model.getAccessPointValue() );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task project()
   {
      final SelectLinkDialog dialog = projectDialog.use( model.getPossibleProjects() ).newInstance();
      dialogs.showOkCancelHelpDialog( projectButton, dialog, i18n.text( WorkspaceResources.choose_project ) );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeProject(dialog.getSelectedLink());
            }
         }
      };
   }

   @Action
   public Task casetype()
   {
      final SelectLinkDialog dialog = caseTypeDialog.use(
            i18n.text( WorkspaceResources.choose_casetype ),
            model.getPossibleCaseTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( caseTypeButton, dialog );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeCaseType(dialog.getSelectedLink());
            }
         }
      };

   }

   @Action
   public Task form()
   {
      final SelectLinkDialog dialog = formDialog.use(
            model.getPossibleForms() ).newInstance();
      dialogs.showOkCancelHelpDialog( formButton, dialog,
            i18n.text( WorkspaceResources.choose_form ) );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.changeForm(dialog.getSelectedLink());
            }
         }
      };

   }

   @Action
   public Task template()
   {
      final SelectLinkDialog dialog = templateDialog.use(
            model.getPossibleTemplates() ).newInstance();

      dialogs.showOkCancelHelpDialog( templateButton, dialog, i18n.text( WorkspaceResources.choose_template ));

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            if (dialog.getSelectedLink() != null)
            {
               model.setTemplate( dialog.getSelectedLink() );
            }
         }
      };

   }

   public void update( Observable o, Object arg )
   {
      if (o == accessPointBinder)
      {
         final Property property = (Property) arg;
         if (property.qualifiedName().name().equals( "template" ))
         {
            new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.setTemplate( null );
               }
            }.execute();
         }
      } else
      {
         accessPointBinder.updateWith( model.getAccessPointValue() );
         updateEnabled();
      }
   }

   private void updateEnabled()
   {
      if (model.getAccessPointValue().project().get() == null)
      {
         caseTypeButton.setEnabled( false );
         labelButton.setEnabled( false );
         formButton.setEnabled( false );
      } else if (model.getAccessPointValue().caseType().get() == null)
      {
         caseTypeButton.setEnabled( true );
         labelButton.setEnabled( false );
         formButton.setEnabled( false );
      } else
      {
         caseTypeButton.setEnabled( true );
         labelButton.setEnabled( true );
         formButton.setEnabled( true );
      }

   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "addedLabel",
            "removedLabel", "addedCaseType", "addedProject",
            "addedSelectedForm", "changedProject", "changedCaseType",
            "formPdfTemplateSet" ), transactions ))
      {
         model.refresh();
      }
   }
}
