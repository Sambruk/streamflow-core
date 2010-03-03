/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.FilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import static se.streamsource.streamflow.domain.interaction.gtd.States.ACTIVE;

/**
 * JAVADOC
 */
public class TaskGeneralView extends JScrollPane implements Observer
{
   @Service
   DialogService dialogs;

   @Service
   UncaughtExceptionHandler exception;

   @Uses
   protected ObjectBuilder<FilterListDialog> taskTypeDialog;

   @Uses
   protected ObjectBuilder<TaskLabelsDialog> labelSelectionDialog;

   private StateBinder taskBinder;

   TaskGeneralModel model;

   public ValueBuilder<TaskGeneralDTO> valueBuilder;
   public JTextField descriptionField;
   private JScrollPane notePane;
   public JXDatePicker dueOnField;
   public JPanel rightForm;
   public JPanel bottomForm;
   public TaskLabelsView labels;
   public PossibleFormsView forms;
   public RefreshWhenVisible refresher;
   public JLabel selectedTaskType = new JLabel();
   public JButton taskTypeButton;
   public JButton labelButton;

   public TaskGeneralView( @Service ApplicationContext appContext,
                           @Uses TaskLabelsView labels, @Uses PossibleFormsView forms )
   {
      this.labels = labels;
      this.forms = forms;
      getVerticalScrollBar().setUnitIncrement( 30 );

      setActionMap( appContext.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            TaskGeneralView.class, this ) );

      taskBinder = new StateBinder();
      taskBinder.addConverter( new StateBinder.Converter()
      {
         public Object toComponent( Object value )
         {
            if (value instanceof ListItemValue)
            {
               return ((ListItemValue) value).description().get();
            } else
               return value;
         }

         public Object fromComponent( Object value )
         {
            return value;
         }
      } );
      taskBinder.setResourceMap( appContext.getResourceMap( getClass() ) );
      TaskGeneralDTO template = taskBinder
            .bindingTemplate( TaskGeneralDTO.class );

      // Layout and form for the right panel
      FormLayout rightLayout = new FormLayout( "80dlu, 5dlu, 150:grow", "pref, pref, pref, pref, pref, pref, pref, pref" );

      rightForm = new JPanel( rightLayout );
      rightForm.setFocusable( false );
      DefaultFormBuilder rightBuilder = new DefaultFormBuilder( rightLayout,
            rightForm );
      rightBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      selectedTaskType.setFont( selectedTaskType.getFont().deriveFont(
            Font.BOLD ) );
      taskBinder.bind( selectedTaskType, template.taskType() );
      CellConstraints cc = new CellConstraints();
      ActionMap am = getActionMap();

      // Description
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( taskBinder.bind( descriptionField = (JTextField) TEXTFIELD.newField(), template.description() ) );
      rightBuilder.nextLine();

      // Select task type
      javax.swing.Action taskTypeAction = am.get( "tasktype" );
      taskTypeButton = new JButton( taskTypeAction );
      taskTypeButton.registerKeyboardAction( taskTypeAction, (KeyStroke) taskTypeAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      taskTypeButton.setHorizontalAlignment( SwingConstants.LEFT );
      rightBuilder.setExtent( 1, 1 );
      rightBuilder.add( taskTypeButton );
      rightBuilder.nextColumn();
      rightBuilder.nextColumn();
      rightBuilder.add( selectedTaskType );
      rightBuilder.nextLine();

      // Select labels
      javax.swing.Action labelAction = am.get( "label" );
      labelButton = new JButton( labelAction );
//		NotificationGlassPane.registerButton(labelButton);
      labelButton.registerKeyboardAction( labelAction, (KeyStroke) labelAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      labelButton.setHorizontalAlignment( SwingConstants.LEFT );
      rightBuilder.add( labelButton );
      rightBuilder.nextLine();
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( labels );
      rightBuilder.nextLine();

      // Due date
      rightBuilder.setExtent( 1, 1 );
      rightBuilder.add( new JLabel( i18n.text( WorkspaceResources.due_on_label ) ) );
      rightBuilder.nextColumn();
      rightBuilder.nextColumn();
      rightBuilder.add( taskBinder.bind( dueOnField = (JXDatePicker) DATEPICKER.newField(), template.dueOn() ) );
      dueOnField.setFormats( DateFormat.getDateInstance( DateFormat.MEDIUM, Locale.getDefault() ) );
      rightBuilder.nextLine();

      // Forms
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( new JLabel( i18n.text( WorkspaceResources.forms_label ) ) );
      rightBuilder.nextLine();
      rightBuilder.add( forms );
      rightBuilder.nextLine();

      // Limit pickable dates to future
      Calendar calendar = Calendar.getInstance();
      calendar.setTime( new Date() );
      calendar.add( Calendar.DAY_OF_MONTH, 1 );
      dueOnField.getMonthView().setLowerBound( calendar.getTime() );



      // Layout and form for the bottom panel
      FormLayout bottomLayout = new FormLayout( "250dlu:grow",
            "15dlu,fill:pref:grow" );

      bottomForm = new JPanel();
      bottomForm.setFocusable( false );
      DefaultFormBuilder bottomBuilder = new DefaultFormBuilder( bottomLayout,
            bottomForm );
      bottomBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY2,
            Sizes.DLUX8, Sizes.DLUY8, Sizes.DLUX8 ) );

      notePane = (JScrollPane) TEXTAREA.newField();
      notePane.setMinimumSize( new Dimension( 10, 50 ) );

      BindingFormBuilder bottomBindingBuilder = new BindingFormBuilder(
            bottomBuilder, taskBinder );
      bottomBindingBuilder.appendLine( WorkspaceResources.note_label,
            notePane, template.note() );

      JPanel formsContainer = new JPanel( new BorderLayout() );
      formsContainer.add( notePane, BorderLayout.CENTER );
      formsContainer.add( rightForm, BorderLayout.EAST );

/*
      JPanel borderLayoutContainer = new JPanel( new BorderLayout() );
      borderLayoutContainer.add( formsContainer, BorderLayout.NORTH );
      borderLayoutContainer.add( bottomForm, BorderLayout.CENTER );
*/

      setViewportView( formsContainer );

      taskBinder.addObserver( this );

      setFocusTraversalPolicy( new LayoutFocusTraversalPolicy() );
      setFocusCycleRoot( true );
      setFocusable( true );

      addFocusListener( new FocusListener()
      {
         public void focusGained( FocusEvent e )
         {
            Component defaultComp = getFocusTraversalPolicy()
                  .getDefaultComponent( notePane );
            if (defaultComp != null)
            {
               defaultComp.requestFocusInWindow();
            }
         }

         public void focusLost( FocusEvent e )
         {
         }
      } );

      notePane.getViewport().getView().setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      notePane.getViewport().getView().setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   public void setModel( TaskGeneralModel taskGeneralModel )
   {
      if (model != null)
         model.deleteObserver( this );

      model = taskGeneralModel;

      refresher.setRefreshable( model );

      TaskGeneralDTO general = model.getGeneral();
      valueBuilder = general.buildWith();
      taskBinder.updateWith( general );

      labels.setLabelsModel( model.labelsModel() );
      forms.setFormsModel( model.formsModel() );

      ListItemValue value = general.taskType().get();
      selectedTaskType
            .setText( value == null ? "" : value.description().get() );

      taskGeneralModel.addObserver( this );

      updateEnabled();
   }

   private void updateEnabled()
   {
      dueOnField.setEnabled( model.getCommandEnabled( "changedueon" ) );
      descriptionField.setEnabled( model.getCommandEnabled( "changedescription" ));
      notePane.getViewport().getView().setEnabled( model.getCommandEnabled( "changenote" ));
      taskTypeButton.setEnabled( model.getCommandEnabled( "tasktype" ));

      labelButton.setEnabled( model.getTaskStatus().equals( ACTIVE ));
      labels.setEnabled( model.getTaskStatus().equals( ACTIVE ));
      forms.setEnabled( model.getTaskStatus().equals( ACTIVE ));
   }

   public void update( Observable o, Object arg )
   {
      updateEnabled();

      if (o == taskBinder)
      {
         Property property = (Property) arg;
         if (property.qualifiedName().name().equals( "description" ))
         {
            model.changeDescription( (String) property.get() );
         } else if (property.qualifiedName().name().equals( "note" ))
         {
            model.changeNote( (String) property.get() );
         } else if (property.qualifiedName().name().equals( "dueOn" ))
         {
            model.changeDueOn( (Date) property.get() );
         }
      } else
      {
         ListItemValue value = model.getGeneral().taskType().get();
         selectedTaskType.setText( value == null ? "" : value.description()
               .get() );
         forms.setFormsModel( model.formsModel() );
      }
   }

   @Action
   public void tasktype()
   {
      FilterListDialog dialog = taskTypeDialog.use(
            i18n.text( WorkspaceResources.chose_tasktype ),
            model.getPossibleTaskTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( taskTypeButton, dialog );

      if (dialog.getSelected() != null)
      {
         model.taskType( dialog.getSelected() );
         // refresh();
      }
   }

   @Action
   public void label()
   {
      TaskLabelsDialog dialog = labelSelectionDialog.use(
            model.getPossibleLabels() ).newInstance();
      dialogs.showOkCancelHelpDialog( labelButton, dialog );

      if (dialog.getSelectedLabels() != null)
      {
         for (LinkValue listItemValue : dialog.getSelectedLabels())
         {
            model.addLabel( EntityReference.parseEntityReference( listItemValue.id().get() ) );
         }
      }
   }
}