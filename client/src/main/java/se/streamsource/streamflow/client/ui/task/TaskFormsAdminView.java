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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;


/**
 * JAVADOC
 */
public class TaskFormsAdminView
      extends JPanel
{

   private TaskSubmittedFormsAdminView submittedFormsView;
   private TaskEffectiveFieldsValueView effectiveFieldsValueView;
   public RefreshWhenVisible refresher;
   public JTabbedPane tabs;

   public TaskFormsAdminView( @Uses TaskEffectiveFieldsValueView effectiveFieldsValueView,
                              @Uses TaskSubmittedFormsAdminView submittedFormsView )
   {
      setLayout( new BorderLayout() );

      tabs = new JTabbedPane();
      add( tabs, BorderLayout.CENTER );

      this.submittedFormsView = submittedFormsView;
      this.effectiveFieldsValueView = effectiveFieldsValueView;
      tabs.addTab( i18n.text( WorkspaceResources.effective_fields_tab ), effectiveFieldsValueView );
      tabs.addTab( i18n.text( WorkspaceResources.submitted_forms_tab ), submittedFormsView );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }

   public TaskSubmittedFormsAdminView getSubmittedFormsView()
   {
      return submittedFormsView;
   }

   public TaskEffectiveFieldsValueView getEffectiveFieldsValueView()
   {
      return effectiveFieldsValueView;
   }

   public void setModel( TaskFormsModel model )
   {
      refresher.setRefreshable( model );

      submittedFormsView.setModel( model.submittedForms() );
      effectiveFieldsValueView.setModel( model.effectiveValues() );
   }
}