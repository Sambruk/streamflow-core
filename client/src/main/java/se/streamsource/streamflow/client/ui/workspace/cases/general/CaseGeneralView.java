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

import ca.odell.glazedlists.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.calendar.*;
import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.property.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;
import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.DRAFT;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;
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
   private Box leftForm;
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
      FormLayout rightLayout = new FormLayout( "70dlu, 2dlu, 200:grow", "pref, pref, pref, pref, 20dlu, pref, pref" );

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
      descriptionField.setName("txtCaseDescription");
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
      labelButton.addActionListener( new ActionListener()
      {

         public void actionPerformed( ActionEvent e )
         {
            labelButton.requestFocusInWindow();
         }
      } );
      labels.setButtonRelation( labelButton );
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

      JPanel formsPanel = new JPanel( new BorderLayout() );
      formsPanel.add( forms, BorderLayout.WEST );
      rightBuilder.add( formsPanel,
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

      leftForm = Box.createVerticalBox();

      notePane = (JScrollPane) TEXTAREA.newField();
      notePane.setMinimumSize( new Dimension( 10, 50 ) );
      notePane.setPreferredSize( new Dimension( 700, 300 ) );
      refreshComponents.enabledOn( "changenote", notePane.getViewport().getView() );

      leftForm.add(new JLabel(i18n.text( WorkspaceResources.note_label ), JLabel.LEFT));
      leftForm.add(notePane);
      actionBinder.bind( "changeNote", notePane );
      valueBinder.bind( "note", notePane );
      
      JPanel formsContainer = new JPanel();
      formsContainer.setLayout( new BoxLayout(formsContainer, BoxLayout.X_AXIS) );
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

      boolean enabled = model.getCaseStatus().equals( DRAFT ) || model.getCaseStatus().equals( OPEN );
      labelButton.setEnabled( enabled );
      labels.setEnabled( enabled );
      forms.setEnabled( enabled );

      valueBinder.update( model.getGeneral() );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDescription( final ActionEvent event )
   {
      Property<String> description = model.getGeneral().description();
      String oldValue = description.get();
      try
      {
         description.set( descriptionField.getText() );
         // set back old value to not mess up model execution
         description.set( oldValue );
      } catch ( ConstraintViolationException cve )
      {
         int maxLength = description.metaInfo( MaxLength.class ).value();
         descriptionField.setText( descriptionField.getText().substring( 0, maxLength ) );
         throw new RuntimeException( new MessageFormat( i18n.text( StreamflowResources.max_length ) ).format( new Object[]{maxLength} ).toString() );
      }
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