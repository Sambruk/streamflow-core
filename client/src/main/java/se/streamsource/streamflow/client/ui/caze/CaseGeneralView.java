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
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.GroupedFilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.caze.CaseGeneralDTO;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.*;

/**
 * JAVADOC
 */
public class CaseGeneralView extends JScrollPane implements Observer
{
   @Service
   DialogService dialogs;

   @Service
   UncaughtExceptionHandler exception;

   @Uses
   protected ObjectBuilder<GroupedFilterListDialog> caseTypeDialog;

   @Uses
   protected ObjectBuilder<CaseLabelsDialog> labelSelectionDialog;

   private StateBinder caseBinder;

   CaseGeneralModel model;

   public ValueBuilder<CaseGeneralDTO> valueBuilder;
   public JTextField descriptionField;
   private JScrollPane notePane;
   public JXDatePicker dueOnField;
   public JPanel rightForm;
   public JPanel bottomForm;
   public CaseLabelsView labels;
   public PossibleFormsView forms;
   public RefreshWhenVisible refresher;
   public RemovableLabel selectedCaseType = new RemovableLabel();
   public JButton caseTypeButton;
   public JButton labelButton;

   public CaseGeneralView( @Service ApplicationContext appContext,
                           @Uses CaseLabelsView labels, @Uses PossibleFormsView forms )
   {
      this.labels = labels;
      this.forms = forms;
      this.setBorder(BorderFactory.createEmptyBorder());
      getVerticalScrollBar().setUnitIncrement( 30 );

      setActionMap( appContext.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            CaseGeneralView.class, this ) );

      caseBinder = new StateBinder();
      caseBinder.addConverter( new StateBinder.Converter()
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
      caseBinder.setResourceMap( appContext.getResourceMap( getClass() ) );
      CaseGeneralDTO template = caseBinder
            .bindingTemplate( CaseGeneralDTO.class );

      // Layout and form for the right panel
      FormLayout rightLayout = new FormLayout( "80dlu, 5dlu, 150:grow", "pref, pref, pref, pref, pref, pref, pref, pref" );

      rightForm = new JPanel( rightLayout );
      rightForm.setFocusable( false );
      DefaultFormBuilder rightBuilder = new DefaultFormBuilder( rightLayout,
            rightForm );
      rightBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      selectedCaseType.setFont( selectedCaseType.getFont().deriveFont(
            Font.BOLD ) );
      caseBinder.bind( selectedCaseType, template.caseType() );
      CellConstraints cc = new CellConstraints();
      ActionMap am = getActionMap();

      // Description
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( caseBinder.bind( descriptionField = (JTextField) TEXTFIELD.newField(), template.description() ) );
      rightBuilder.nextLine();

      // Select case type
      javax.swing.Action caseTypeAction = am.get( "casetype" );
      caseTypeButton = new JButton( caseTypeAction );
      caseTypeButton.registerKeyboardAction( caseTypeAction, (KeyStroke) caseTypeAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      caseTypeButton.setHorizontalAlignment( SwingConstants.LEFT );
      rightBuilder.setExtent( 1, 1 );
      rightBuilder.add( caseTypeButton );
      //rightBuilder.nextColumn();
      //rightBuilder.nextColumn();
      rightBuilder.add( selectedCaseType,
            new CellConstraints( 3,2,1,1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets(5,0,0,0) ) );
      
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
      rightBuilder.add( caseBinder.bind( dueOnField = (JXDatePicker) DATEPICKER.newField(), template.dueOn() ) );
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
            bottomBuilder, caseBinder );
      bottomBindingBuilder.appendLine( WorkspaceResources.note_label,
            notePane, template.note() );

      JPanel formsContainer = new JPanel( new GridLayout(1,2) );
      formsContainer.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));
      formsContainer.add( notePane );
      formsContainer.add( rightForm );

/*
      JPanel borderLayoutContainer = new JPanel( new BorderLayout() );
      borderLayoutContainer.add( formsContainer, BorderLayout.NORTH );
      borderLayoutContainer.add( bottomForm, BorderLayout.CENTER );
*/

      setViewportView( formsContainer );

      caseBinder.addObserver( this );

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

   public void setModel( CaseGeneralModel caseGeneralModel )
   {
      if (model != null)
         model.deleteObserver( this );

      model = caseGeneralModel;

      refresher.setRefreshable( model );

      CaseGeneralDTO general = model.getGeneral();
      valueBuilder = general.buildWith();
      caseBinder.updateWith( general );

      labels.setLabelsModel( model.labelsModel() );
      forms.setFormsModel( model.formsModel() );

      ListItemValue value = general.caseType().get();
      selectedCaseType
            .setListItemValue( value );

      selectedCaseType.setVisible( value == null ? false : true );
      

      caseGeneralModel.addObserver( this );

      updateEnabled();
   }

   private void updateEnabled()
   {
      dueOnField.setEnabled( model.getCommandEnabled( "changedueon" ) );
      descriptionField.setEnabled( model.getCommandEnabled( "changedescription" ));
      notePane.getViewport().getView().setEnabled( model.getCommandEnabled( "changenote" ));
      caseTypeButton.setEnabled( model.getCommandEnabled( "casetype" ));
      selectedCaseType.setEnabled( model.getCommandEnabled(  "casetype" ));

      boolean enabled = model.getCaseStatus().equals( DRAFT ) || model.getCaseStatus().equals( OPEN );
      labelButton.setEnabled( enabled );
      labels.setEnabled( enabled );
      forms.setEnabled( enabled );
   }

   public void update( Observable o, Object arg )
   {
      if (o == caseBinder)
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
         } else if (property.qualifiedName().name().equals( "caseType" ))
         {
            model.caseType( null );
            selectedCaseType.setVisible( false );
         }
      } else                                              
      {
         CaseGeneralDTO general = model.getGeneral();
         valueBuilder = general.buildWith();
         caseBinder.updateWith( general );

         forms.setFormsModel( model.formsModel() );
      }

      updateEnabled();
   }

   @Action
   public void casetype()
   {
      GroupedFilterListDialog dialog = caseTypeDialog.use(
            i18n.text( WorkspaceResources.chose_casetype ),
            model.getPossibleCaseTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( caseTypeButton, dialog );

      if (dialog.getSelectedReference() != null)
      {
         model.caseType( dialog.getSelectedReference() );
         // refresh();
      }
   }

   @Action
   public void label()
   {
      CaseLabelsDialog dialog = labelSelectionDialog.use(
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