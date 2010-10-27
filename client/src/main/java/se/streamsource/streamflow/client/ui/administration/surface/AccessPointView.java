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

package se.streamsource.streamflow.client.ui.administration.surface;

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
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.ui.workspace.caze.general.CaseLabelsDialog;
import se.streamsource.streamflow.client.ui.workspace.caze.general.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.caze.RemovableLabel;
import se.streamsource.streamflow.client.util.FilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;


public class AccessPointView
      extends JPanel
      implements Observer, TransactionListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   @Uses
   protected ObjectBuilder<FilterListDialog> projectDialog;

   @Uses
   protected ObjectBuilder<FilterListDialog> caseTypeDialog;

   @Uses
   protected ObjectBuilder<FilterListDialog> formDialog;

   @Uses
   protected ObjectBuilder<FilterListDialog> templateDialog;

   @Uses
   protected ObjectBuilder<CaseLabelsDialog> labelSelectionDialog;

   public CaseLabelsView labels;
   public JLabel selectedCaseType = new JLabel();
   public JButton caseTypeButton;
   public JButton labelButton;
   public JButton projectButton;
   public JLabel selectedProject = new JLabel();
   public JButton formButton;
   public JLabel selectedForm = new JLabel();

   JButton templateButton;
   RemovableLabel selectedTemplate = new RemovableLabel();

   private AccessPointModel model;

   private StateBinder accessPointBinder;

   public AccessPointView( @Service ApplicationContext appContext,
                           @Uses CommandQueryClient client,
                           @Structure ObjectBuilderFactory obf )
   {
      this.labels = obf.newObjectBuilder( CaseLabelsView.class ).use( client.getSubClient( "labels" )).newInstance();
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
      AccessPointValue template = accessPointBinder
            .bindingTemplate( AccessPointValue.class );

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
      javax.swing.Action labelAction = am.get( "label" );
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

      new RefreshWhenVisible( this, model );
// TODO      labels.setLabelsModel( model.labelsModel() );
   }

   @Action
   public Task project()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            FilterListDialog dialog = projectDialog.use(
                  i18n.text( WorkspaceResources.choose_project ),
                  model.getPossibleProjects() ).newInstance();
            dialogs.showOkCancelHelpDialog( projectButton, dialog );

            if (dialog.getSelected() != null)
            {
               model.setProject( dialog.getSelected().identity() );
            }
         }
      };

   }

   @Action
   public Task casetype()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            FilterListDialog dialog = caseTypeDialog.use(
                  i18n.text( WorkspaceResources.chose_casetype ),
                  model.getPossibleCaseTypes() ).newInstance();
            dialogs.showOkCancelHelpDialog( caseTypeButton, dialog );

            if (dialog.getSelected() != null)
            {
               model.setCaseType( dialog.getSelected().identity() );
            }
         }
      };

   }

   @Action
   public Task label()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            CaseLabelsDialog dialog = labelSelectionDialog.use(
                  model.getPossibleLabels() ).newInstance();
            dialogs.showOkCancelHelpDialog( labelButton, dialog );

            if (dialog.getSelectedLabels() != null)
            {
               for (LinkValue listItemValue : dialog.getSelectedLabels())
               {
                  model.labelsModel().addLabel( listItemValue );
               }
            }
         }
      };

   }

   @Action
   public Task form()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            FilterListDialog dialog = formDialog.use(
                  i18n.text( WorkspaceResources.choose_form ),
                  model.getPossibleForms() ).newInstance();
            dialogs.showOkCancelHelpDialog( formButton, dialog );

            if (dialog.getSelected() != null)
            {
               model.setForm( dialog.getSelected().identity() );
            }
         }
      };

   }

   @Action
   public Task template()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            FilterListDialog dialog = templateDialog.use(
                  i18n.text( WorkspaceResources.choose_template ),
                  model.getPossibleTemplates() ).newInstance();

            dialogs.showOkCancelHelpDialog( templateButton, dialog );

            if (dialog.getSelected() != null)
            {
               model.setTemplate( dialog.getSelected().identity() );
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
                  model.removeTemplate();
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

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames( "addedLabel",
            "removedLabel", "addedCaseType", "addedProject",
            "addedSelectedForm", "changedProject", "changedCaseType",
            "selectedTemplateAdded", "selectedTemplateRemoved" ), transactions ))
      {
         model.refresh();
      }
   }
}
