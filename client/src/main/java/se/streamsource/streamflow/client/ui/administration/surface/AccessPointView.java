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
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsDialog;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsView;
import se.streamsource.streamflow.client.ui.workspace.FilterListDialog;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;

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
      implements Observer
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   @Service
   UncaughtExceptionHandler exception;

   @Uses
   protected ObjectBuilder<FilterListDialog> projectDialog;

   @Uses
   protected ObjectBuilder<FilterListDialog> caseTypeDialog;

   @Uses
   protected ObjectBuilder<CaseLabelsDialog> labelSelectionDialog;

   public CaseLabelsView labels;
   public RefreshWhenVisible refresher;
   public JLabel selectedCaseType = new JLabel();
   public JButton caseTypeButton;
   public JButton labelButton;
   public JButton projectButton;
   public JLabel selectedProject = new JLabel();

   private AccessPointModel model;

   private StateBinder accessPointBinder;

   public AccessPointView( @Service ApplicationContext appContext,
                           @Uses CaseLabelsView labels,
                           @Uses AccessPointModel model )
   {
      this.model = model;
      model.addObserver( this );

      setLayout( new BorderLayout() );

      accessPointBinder = new StateBinder();
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

      FormLayout layout = new FormLayout( "50dlu, 5dlu, 150:grow", "pref, 2dlu, pref, 2dlu, default:grow" );

      JPanel panel = new JPanel( layout );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            panel );
      builder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      CellConstraints cc = new CellConstraints();

      setActionMap( appContext.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            AccessPointView.class, this ) );


      selectedCaseType.setFont( selectedCaseType.getFont().deriveFont(
            Font.BOLD ) );

      selectedProject.setFont( selectedProject.getFont().deriveFont( Font.BOLD ) );

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

      labels.setPreferredSize( new Dimension( 500, 200 ) );
      builder.add( labels,
            new CellConstraints( 3, 5, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 5, 0, 0, 0 ) ) );

      add( panel, BorderLayout.CENTER );

      accessPointBinder.updateWith( model.getAccessPointValue() );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
      refresher.setRefreshable( model );
      labels.setLabelsModel( model.labelsModel() );
   }

   @Action
   public void project()
   {

      FilterListDialog dialog = projectDialog.use(
            i18n.text( WorkspaceResources.chose_project ),
            model.getPossibleProjects() ).newInstance();
      dialogs.showOkCancelHelpDialog( caseTypeButton, dialog );

      if (dialog.getSelected() != null)
      {
         model.setProject( dialog.getSelected().identity() );
      }
   }

   @Action
   public void casetype()
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
            model.labelsModel().addLabel( EntityReference.parseEntityReference( listItemValue.id().get() ) );
         }
      }
   }

   public void update( Observable o, Object arg )
   {
      accessPointBinder.updateWith( model.getAccessPointValue() );
      updateEnabled();
   }

   private void updateEnabled()
   {
      if (model.getAccessPointValue().project().get() == null)
      {
         caseTypeButton.setEnabled( false );
         labelButton.setEnabled( false );
      } else if (model.getAccessPointValue().caseType().get() == null)
      {
         caseTypeButton.setEnabled( true );
         labelButton.setEnabled( false );
      } else
      {
         caseTypeButton.setEnabled( true );
         labelButton.setEnabled( true );
      }

   }
}
