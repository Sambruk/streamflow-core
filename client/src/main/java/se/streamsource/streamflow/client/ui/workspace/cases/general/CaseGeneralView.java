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

package se.streamsource.streamflow.client.ui.workspace.cases.general;

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.DRAFT;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.DATEPICKER;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTFIELD;
import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
import javax.swing.text.DefaultFormatterFactory;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.DatePickerFormatter;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.caselog.CaseLogView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsView;
import se.streamsource.streamflow.client.ui.workspace.cases.note.CaseNoteView;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import ca.odell.glazedlists.EventList;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * JAVADOC
 */
public class CaseGeneralView extends JScrollPane implements TransactionListener, Refreshable
{
   @Service
   private DialogService dialogs;

   @Service
   private UncaughtExceptionHandler exception;

   @Structure
   Module module;

   private ActionBinder actionBinder;
   private ValueBinder valueBinder;

   private CaseGeneralModel model;

   private JTextField descriptionField;
   private JXDatePicker dueOnField;
   private JPanel leftPane;
   private CaseLabelsView labels;
   private PossibleFormsView forms;
   private RemovableLabel selectedCaseType = new RemovableLabel();
   private JButton caseTypeButton;
   private JButton labelButton;
   private final ApplicationContext appContext;
   private CaseNoteView caseNotes;

   public CaseGeneralView(@Service ApplicationContext appContext, @Uses CaseGeneralModel generalModel,
         @Uses CaseLogView caseLogView, @Structure Module module)
   {
      this.appContext = appContext;
      this.model = generalModel;
      RefreshComponents refreshComponents = new RefreshComponents();
      model.addObserver( refreshComponents );
      ObjectBuilderFactory obf = module.objectBuilderFactory();
      this.labels = obf.newObjectBuilder( CaseLabelsView.class ).use( generalModel.newLabelsModel() ).newInstance();
      this.caseNotes = obf.newObjectBuilder( CaseNoteView.class ).use( generalModel.newCaseNoteModel() ).newInstance();

      RefreshComponents refreshLabelComponents = new RefreshComponents();
      labels.getModel().addObserver( refreshLabelComponents );

      this.forms = obf.newObjectBuilder( PossibleFormsView.class ).use( generalModel.newPossibleFormsModel() )
            .newInstance();
      refreshComponents.visibleOn( "changedescription", forms );
      this.setBorder( BorderFactory.createEmptyBorder() );
      getVerticalScrollBar().setUnitIncrement( 30 );

      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap( CaseGeneralView.class, this ) );

      actionBinder = obf.newObjectBuilder( ActionBinder.class ).use( am ).newInstance();
      valueBinder = obf.newObject( ValueBinder.class );
      actionBinder.setResourceMap( appContext.getResourceMap( getClass() ) );

      // Layout and form for the right panel
      FormLayout leftLayout = new FormLayout( "50dlu, 2dlu, 200:grow, 70dlu",
            "pref, pref, pref, pref, 20dlu, pref, pref, pref, pref" );

      leftPane = new JPanel( leftLayout );
      leftPane.setFocusable( false );
      DefaultFormBuilder leftBuilder = new DefaultFormBuilder( leftLayout, leftPane );
      leftBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY2, Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX11 ) );

      selectedCaseType.getLabel().setFont( selectedCaseType.getLabel().getFont().deriveFont( Font.BOLD ) );
      selectedCaseType.getButton().addActionListener( am.get( "removeCaseType" ) );
      valueBinder.bind( "caseType", selectedCaseType );

      // Description & DueDate
      leftBuilder.setExtent( 3, 1 );
      JLabel descriptionLabel = leftBuilder.getComponentFactory().createLabel(
            i18n.text( WorkspaceResources.description_label ) );
      leftBuilder.add( descriptionLabel );
      descriptionLabel.setBorder( BorderFactory.createEmptyBorder( 0, 2, 0, 0 ) );
      leftBuilder.nextColumn(3);
      JLabel dueOnLabel = leftBuilder.append( i18n.text( WorkspaceResources.due_on_label ) );
      dueOnLabel.setBorder( BorderFactory.createEmptyBorder( 0, 2, 0, 0 ) );

      leftBuilder.nextLine();
      leftBuilder.setExtent( 3, 1 );
      JPanel descPanel = new JPanel( new BorderLayout() );
      descPanel.add(
            valueBinder.bind( "description",
                  actionBinder.bind( "changeDescription", descriptionField = (JTextField) TEXTFIELD.newField() ) ),
            BorderLayout.WEST );
      leftBuilder.add( descPanel );
      descriptionField.setName( "txtCaseDescription" );
      leftBuilder.add(
            valueBinder.bind( "dueOn",
                  actionBinder.bind( "changeDueOn", dueOnField = (JXDatePicker) DATEPICKER.newField() ) ),
            new CellConstraints( 4, 2, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets( 4, 0, 0, 0 ) ) );
      leftBuilder.nextLine();
      descriptionLabel.setLabelFor( descriptionField );
      dueOnLabel.setLabelFor( dueOnField );
      refreshComponents.enabledOn( "changedescription", descriptionField );
      refreshComponents.enabledOn( "changedueon", dueOnField );

      // Select case type
      javax.swing.Action caseTypeAction = am.get( "changeCaseType" );
      caseTypeButton = new JButton( caseTypeAction );
      caseTypeButton.registerKeyboardAction( caseTypeAction,
            (KeyStroke) caseTypeAction.getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      caseTypeButton.setHorizontalAlignment( SwingConstants.LEFT );
      refreshComponents.enabledOn( "casetype", caseTypeButton, selectedCaseType );

      leftBuilder.add( caseTypeButton, new CellConstraints( 1, 3, 1, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 2, 0, 5, 0 ) ) );
      leftBuilder.add( selectedCaseType, new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM,
            new Insets( 5, 0, 0, 0 ) ) );

      leftBuilder.nextLine();

      // Select labels
      javax.swing.Action labelAction = labels.getActionMap().get( "addLabel" );
      labelButton = new JButton( labelAction );
      // NotificationGlassPane.registerButton(labelButton);
      labelButton.registerKeyboardAction( labelAction,
            (KeyStroke) labelAction.getValue( javax.swing.Action.ACCELERATOR_KEY ), JComponent.WHEN_IN_FOCUSED_WINDOW );

      labelButton.setHorizontalAlignment( SwingConstants.LEFT );
      labelButton.addActionListener( new ActionListener()
      {

         public void actionPerformed(ActionEvent e)
         {
            labelButton.requestFocusInWindow();
         }
      } );
      labels.setButtonRelation( labelButton );
      refreshLabelComponents.enabledOn( "addlabel", labelButton, labels );

      leftBuilder.add( labelButton, new CellConstraints( 1, 4, 1, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 5, 0, 0, 0 ) ) );

      labels.setPreferredSize( new Dimension( 500, 80 ) );
      leftBuilder.add( labels, new CellConstraints( 3, 4, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets(
            5, 0, 0, 0 ) ) );
      leftBuilder.nextLine();

      
      leftBuilder.add( caseNotes, new CellConstraints( 1, 6, 4, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 0, 2, 5, 0 ) ) );
      
      // Forms
      JLabel formsLabel = new JLabel( i18n.text( WorkspaceResources.forms_label ) );
      refreshComponents.visibleOn( "changedescription", formsLabel );
      leftBuilder.add( formsLabel, new CellConstraints( 1, 7, 1, 1, CellConstraints.LEFT, CellConstraints.TOP,
            new Insets( 5, 0, 0, 0 ) ) );
      leftBuilder.nextLine();

      JPanel formsPanel = new JPanel( new BorderLayout() );
      formsPanel.add( forms, BorderLayout.WEST );
      leftBuilder.add( formsPanel, new CellConstraints( 1, 8, 3, 1, CellConstraints.FILL, CellConstraints.FILL,
            new Insets( 5, 0, 0, 0 ) ) );

      // Limit pickable dates to future
      Calendar calendar = Calendar.getInstance();
      calendar.setTime( new Date() );
      calendar.add( Calendar.DAY_OF_MONTH, 1 );
      dueOnField.getMonthView().setLowerBound( calendar.getTime() );

      final DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.SHORT );
      dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
      dueOnField.getEditor().setFormatterFactory(
            new DefaultFormatterFactory( new DatePickerFormatter( new DateFormat[]
            { dateFormat } )
            {
               @Override
               public Object stringToValue(String text) throws ParseException
               {
                  Object result;
                  try
                  {
                     result = super.stringToValue( text );
                  } catch (ParseException pe)
                  {
                     dialogs.showMessageDialog( dueOnField, text( WorkspaceResources.wrong_format_msg ) + " "
                           + ((SimpleDateFormat) dateFormat).toPattern(), text( WorkspaceResources.wrong_format_title ) );
                     throw pe;
                  }
                  return result;
               }
            } ) );
      
      // Main panel that contains both left and right pane
      JPanel formsContainer = new JPanel();
      formsContainer.setLayout( new GridLayout( 1, 2 ) );
      formsContainer.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );
      formsContainer.add( leftPane );
      formsContainer.add( caseLogView );

      setViewportView( formsContainer );

      setFocusTraversalPolicy( new LayoutFocusTraversalPolicy() );
      setFocusCycleRoot( true );
      setFocusable( true );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();

      boolean enabled = model.getCaseStatus().equals( DRAFT ) || model.getCaseStatus().equals( OPEN );
      labelButton.setEnabled( enabled );
      labels.setEnabled( enabled );
      forms.setEnabled( enabled );

      valueBinder.update( model.getGeneral() );
      selectedCaseType.setClickLink( model.getGeneral().caseType().get() );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDescription(final ActionEvent event)
   {
      Property<String> description = model.getGeneral().description();
      String oldValue = description.get();
      try
      {
         description.set( descriptionField.getText() );
         // set back old value to not mess up model execution
         description.set( oldValue );
      } catch (ConstraintViolationException cve)
      {
         int maxLength = description.metaInfo( MaxLength.class ).value();
         descriptionField.setText( descriptionField.getText().substring( 0, maxLength ) );
         throw new RuntimeException( new MessageFormat( i18n.text( StreamflowResources.max_length ) ).format(
               new Object[]
               { maxLength } ).toString() );
      }
      return new CommandTask()
      {
         @Override
         public void command() throws Exception
         {
            model.changeDescription( descriptionField.getText() );
         }
      };
   }

  
   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDueOn( final ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command() throws Exception
         {
            model.changeDueOn( dueOnField.getDate() );
         }
      };
   }

   @Action
   public Task changeCaseType()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder( SelectLinkDialog.class )
            .use( model.getPossibleCaseTypes() ).newInstance();
      dialogs.showOkCancelHelpDialog( caseTypeButton, dialog, i18n.text( WorkspaceResources.choose_casetype ) );

      caseTypeButton.requestFocusInWindow();

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               LinkValue selected = dialog.getSelectedLink();
               model.changeCaseType( selected );

               // selectedCaseType.setRemoveLink(selected);

               String labelQuery = dialog.getFilterField().getText();
               // if the query string has any match inside label descriptions
               // we do a search for that labels and add them to the case
               // automatically
               if (!"".equals( labelQuery )
                     && selected.classes().get().toLowerCase().indexOf( labelQuery.toLowerCase() ) != -1)
               {
                  EventList<LinkValue> possibleLabels = labels.getModel().getPossibleLabels();
                  for (LinkValue link : possibleLabels)
                  {
                     if (link.text().get().toLowerCase().contains( labelQuery.toLowerCase() ))
                     {
                        labels.getModel().addLabel( link );
                     }
                  }
               }

            }
         };
      } else
         return null;
   }

   @Action
   public Task removeCaseType()
   {
      return new CommandTask()
      {
         @Override
         public void command() throws Exception
         {
            model.removeCaseType();
         }
      };
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (matches( withNames( "addedLabel", "removedLabel", "changedOwner", "changedCaseType", "changedStatus" ),
            transactions ))
      {
         refresh();
      }
   }
}