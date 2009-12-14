/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * JAVADOC
 */
public class FormSubmissionWizardView
      extends JPanel
{
   Dimension dialogSize = new Dimension( 600, 300 );

   @Structure
   ObjectBuilderFactory obf;

   private FormSubmissionWizardModel model;
   private FormsListView formsListView;
   private FormSubmitView formSubmitView;

   private CardLayout layout = new CardLayout();
   private JPanel panel;

   public FormSubmissionWizardView( @Service ApplicationContext context,
                                    @Uses final FormsListView formsListView,
                                    @Uses final FormSubmitView formSubmitView,
                                    @Uses FormSubmissionWizardModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setActionMap( context.getActionMap( this ) );
      setPreferredSize( dialogSize );

      this.formSubmitView = formSubmitView;
      this.formsListView = formsListView;

      panel = new JPanel( layout );
      panel.add( formsListView, "SELECTFORM" );
      panel.add( formSubmitView, "INPUTFORM" );
      add( panel, BorderLayout.CENTER );

      JPanel toolbar = new JPanel( new FlowLayout() );
      toolbar.add( model.previousButton( new JButton( am.get( "previous" ) ) ) );
      toolbar.add( model.submitButton( new JButton( am.get( "submit" ) ) ) );
      toolbar.add( new JButton( am.get( "cancel" ) ) );
      toolbar.add( new JButton( am.get( "next" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      formsListView.getFormList().getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "next" ) ) );
      model.initialStep();
   }

   @Action
   public void submit()
   {
      SubmitFormDTO submitDTO = formSubmitView.getSubmitFormDTO();
      model.submitForm( submitDTO );
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void next()
   {
      ListItemValue value = (ListItemValue) formsListView.getFormList().getSelectedValue();
      formsListView.getFormList().clearSelection();

      formSubmitView.setModel( model.getFormSubmitModel( value.entity().get().identity() ) );

      model.nextStep();
      layout.show( panel, "INPUTFORM" );
   }

   @Action
   public void previous()
   {
      model.previousStep();
      layout.show( panel, "SELECTFORM" );
   }
}