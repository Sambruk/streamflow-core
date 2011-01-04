/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.general;

import ca.odell.glazedlists.EventList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.DatePickerFormatter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsView;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;
import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.DRAFT;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.OPEN;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

/**
 * JAVADOC
 */
public class CaseGeneralView extends JScrollPane implements TransactionListener, Refreshable
{
   @Service
   private DialogService dialogs;

   @Service
   private UncaughtExceptionHandler exception;

   @Uses
   private ObjectBuilder<SelectLinkDialog> caseTypeDialog;

   private ActionBinder actionBinder;
   private ValueBinder valueBinder;

   private CaseGeneralModel model;

   private JTextField descriptionField;
   private JScrollPane notePane;
   private JXDatePicker dueOnField;
   private JPanel rightForm;
   private JPanel leftForm;
   private CaseLabelsView labels;
   private PossibleFormsView forms;
   private RemovableLabel selectedCaseType = new RemovableLabel();
   private JButton caseTypeButton;
   private JButton labelButton;
   private final ApplicationContext appContext;

   public CaseGeneralView( @Service ApplicationContext appContext,
                           @Uses CommandQueryClient client,
                           @Structure ObjectBuilderFactory obf )
   {
      this.appContext = appContext;
      this.model = obf.newObjectBuilder( CaseGeneralModel.class ).use( client ).newInstance();
      RefreshComponents refreshComponents = new RefreshComponents();
      model.addObserver( refreshComponents );

      this.labels = obf.newObjectBuilder( CaseLabelsView.class ).use( client.getSubClient( "labels" ) ).newInstance();

      RefreshComponents refreshLabelComponents = new RefreshComponents();
      labels.getModel().addObserver( refreshLabelComponents );

      this.forms = obf.newObjectBuilder( PossibleFormsView.class ).use( client.getClient( "../possibleforms/" ) ).newInstance();
      refreshComponents.visibleOn( "changedescription", forms );
      this.setBorder( BorderFactory.createEmptyBorder() );
      getVerticalScrollBar().setUnitIncrement( 30 );

      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            CaseGeneralView.class, this ) );

      actionBinder = obf.newObjectBuilder( ActionBinder.class ).use( am ).newInstance();
      valueBinder = obf.newObject( ValueBinder.class );
      actionBinder.setResourceMap( appContext.getResourceMap( getClass() ) );

      // Layout and form for the right panel
      FormLayout rightLayout = new FormLayout( "70dlu, 2dlu, 200:grow", "pref, pref, pref, pref, 20dlu, pref, fill:pref:grow" );

      rightForm = new JPanel( rightLayout );
      rightForm.setFocusable( false );
      DefaultFormBuilder rightBuilder = new DefaultFormBuilder( rightLayout,
            rightForm );
      rightBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY2,
            Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX2 ) );

      selectedCaseType.setFont( selectedCaseType.getFont().deriveFont(
            Font.BOLD ) );
      selectedCaseType.getButton().addActionListener(am.get("removeCaseType" ));
      valueBinder.bind( "caseType", selectedCaseType );

      // Description
      rightBuilder.setExtent( 1, 1 );
      JLabel descriptionLabel = rightBuilder.append(i18n.text( WorkspaceResources.description_label ));
      descriptionLabel.setBorder( BorderFactory.createEmptyBorder( 0, 2, 0, 0 ) );
      rightBuilder.nextLine();
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( valueBinder.bind( "description", actionBinder.bind( "changeDescription", descriptionField = (JTextField) TEXTFIELD.newField() ) ) );
      rightBuilder.nextLine();
      descriptionLabel.setLabelFor( descriptionField );
      refreshComponents.enabledOn( "changedescription", descriptionField );

      // Select case type
      javax.swing.Action caseTypeAction = am.get( "changeCaseType" );
      caseTypeButton = new JButton( caseTypeAction );
      caseTypeButton.registerKeyboardAction( caseTypeAction, (KeyStroke) caseTypeAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );
      caseTypeButton.setHorizontalAlignment( SwingConstants.LEFT );
      refreshComponents.enabledOn( "casetype", caseTypeButton, selectedCaseType );

      rightBuilder.add( caseTypeButton,
            new CellConstraints( 1, 3, 1, 1, CellConstraints.FILL, CellConstraints.TOP, new Insets( 2, 0, 5, 0 ) ) );
      rightBuilder.add( selectedCaseType,
            new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets( 5, 0, 0, 0 ) ) );

      rightBuilder.nextLine();

      // Select labels
      javax.swing.Action labelAction = labels.getActionMap().get( "addLabel" );
      labelButton = new JButton( labelAction );
//		NotificationGlassPane.registerButton(labelButton);
      labelButton.registerKeyboardAction( labelAction, (KeyStroke) labelAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      labelButton.setHorizontalAlignment( SwingConstants.LEFT );
      labelButton.addActionListener( new ActionListener(){

         public void actionPerformed( ActionEvent e )
         {
            labelButton.requestFocusInWindow();
         }
      });
      refreshLabelComponents.enabledOn( "addlabel", labelButton, labels );
      
      rightBuilder.add( labelButton,
            new CellConstraints( 1, 4, 1, 1, CellConstraints.FILL, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );

      labels.setPreferredSize( new Dimension( 500, 80 ) );
      rightBuilder.add( labels,
            new CellConstraints( 3, 4, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );
      rightBuilder.nextLine();

      // Due date
      rightBuilder.setExtent( 1, 1 );
      rightBuilder.add( new JLabel( i18n.text( WorkspaceResources.due_on_label ) ),
            new CellConstraints( 1, 5, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 4, 2, 0, 0 ) ) );
      rightBuilder.nextColumn();
      rightBuilder.nextColumn();
      rightBuilder.add( valueBinder.bind( "dueOn", actionBinder.bind( "changeDueOn", dueOnField = (JXDatePicker) DATEPICKER.newField() ) ),
            new CellConstraints( 3, 5, 1, 1, CellConstraints.LEFT, CellConstraints.BOTTOM, new Insets( 4, 2, 0, 0 ) ) );
      rightBuilder.nextLine();
      refreshComponents.enabledOn( "changedueon", dueOnField );


      // Forms
      JLabel formsLabel = new JLabel( i18n.text( WorkspaceResources.forms_label ) );
      refreshComponents.visibleOn( "changedescription", formsLabel);
      rightBuilder.add( formsLabel,
            new CellConstraints( 1, 6, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );

      rightBuilder.add( forms,
            new CellConstraints( 3, 6, 1, 1, CellConstraints.FILL, CellConstraints.FILL, new Insets( 5, 0, 0, 0 ) ) );

      // Limit pickable dates to future
      Calendar calendar = Calendar.getInstance();
      calendar.setTime( new Date() );
      calendar.add( Calendar.DAY_OF_MONTH, 1 );
      dueOnField.getMonthView().setLowerBound( calendar.getTime() );

      final DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.SHORT );
      dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
      dueOnField.getEditor().setFormatterFactory( new DefaultFormatterFactory( new DatePickerFormatter( new DateFormat[]{dateFormat} )
      {

         @Override
         public Object stringToValue( String text ) throws ParseException
         {
            Object result;
            try
            {
               result = super.stringToValue( text );
            } catch (ParseException pe)
            {
               dialogs.showMessageDialog( dueOnField,
                     text( WorkspaceResources.wrong_format_msg ) + " " + ((SimpleDateFormat) dateFormat).toPattern(),
                     text( WorkspaceResources.wrong_format_title ) );
               throw pe;
            }
            return result;
         }
      } ) );


      // Layout and form for the bottom panel
      FormLayout leftLayout = new FormLayout( "200dlu:grow",
            "pref,fill:pref:grow" );

      leftForm = new JPanel();
      leftForm.setPreferredSize( new Dimension( 200, 100 ) );
      leftForm.setFocusable( false );
      DefaultFormBuilder leftBuilder = new DefaultFormBuilder( leftLayout,
            leftForm );
      leftBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY2,
            Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX2 ) );

      notePane = (JScrollPane) TEXTAREA.newField();
      notePane.setMinimumSize( new Dimension( 10, 50 ) );
      refreshComponents.enabledOn( "changenote", notePane.getViewport().getView() );

      BindingFormBuilder2 leftBindingBuilder = new BindingFormBuilder2(
            leftBuilder, actionBinder, valueBinder, appContext.getResourceMap( getClass() ) );
      leftBindingBuilder.appendWithLabel( WorkspaceResources.note_label,
            notePane, "note", "changeNote" );

      JPanel formsContainer = new JPanel( new GridLayout( 1, 2 ) );
      formsContainer.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );
      formsContainer.add( leftForm );
      formsContainer.add( rightForm );

/*
      JPanel borderLayoutContainer = new JPanel( new BorderLayout() );
      borderLayoutContainer.add( formsContainer, BorderLayout.NORTH );
      borderLayoutContainer.add( leftForm, BorderLayout.CENTER );
*/

      setViewportView( formsContainer );

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

      notePane.getViewport().getView().setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null );
      notePane.getViewport().getView().setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();
//      labels.getModel().refresh();

/*
      dueOnField.setEnabled( model.getCommandEnabled( "changedueon" ) );
      descriptionField.setEnabled( model.getCommandEnabled( "changedescription" ) );
      notePane.getViewport().getView().setEnabled( model.getCommandEnabled( "changenote" ) );
      caseTypeButton.setEnabled( model.getCommandEnabled( "casetype" ) );
      selectedCaseType.setEnabled( model.getCommandEnabled( "casetype" ) );
*/

      boolean enabled = model.getCaseStatus().equals( DRAFT ) || model.getCaseStatus().equals( OPEN );
      labelButton.setEnabled( enabled );
      labels.setEnabled( enabled );
      forms.setEnabled( enabled );

      valueBinder.update( model.getGeneral() );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDescription( final ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.changeDescription( descriptionField.getText() );
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeNote( final ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.changeNote( ((JTextArea) event.getSource()).getText() );
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDueOn( final ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.changeDueOn( dueOnField.getDate() );
         }
      };
   }

   @Action
   public Task changeCaseType()
   {
      final SelectLinkDialog dialog = caseTypeDialog.use(
            model.getPossibleCaseTypes() ).newInstance();
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

               selectedCaseType.getLabel().setText( selected.text().get() );

               String labelQuery = dialog.getFilterField().getText();
               // if the query string has any match inside label descriptions
               // we do a search for that labels and add them to the case automatically
               if (!"".equals( labelQuery ) && selected.classes().get().toLowerCase().indexOf( labelQuery.toLowerCase() ) != -1)
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
         public void command()
               throws Exception
         {
            model.removeCaseType();
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames( "addedLabel", "removedLabel", "changedOwner", "changedCaseType", "changedStatus" ), transactions ))
      {
         refresh();
      }
   }
}