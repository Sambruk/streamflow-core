/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.forms;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.TabbedResourceView;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class FormView
      extends JPanel
      implements Observer
{
   private FormModel model;

   private JTextArea textArea;

   @Structure
   Module module;

   public FormView( @Service ApplicationContext context,
                    @Uses FormModel model)
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));
      
      ActionMap am = context.getActionMap( this );

      model.addObserver( this );
      textArea = new JTextArea();
      textArea.setLineWrap( true );
      textArea.setWrapStyleWord( true );
      textArea.setEditable( false );
      add( new JScrollPane(textArea), BorderLayout.CENTER );
      add( new StreamflowButton( am.get( "edit" ) ), BorderLayout.SOUTH );

      new RefreshWhenShowing(this, model);
   }

   @org.jdesktop.application.Action
   public void edit()
   {
      //FormEditView formEditView = obf.newObjectBuilder( FormEditView.class ).use( client ).newInstance();
      TabbedResourceView resourceView = module.objectBuilderFactory().newObjectBuilder(TabbedResourceView.class).use( model ).newInstance();

      AdministrationView adminView = (AdministrationView) SwingUtilities.getAncestorOfClass( AdministrationView.class, this );

      adminView.show( resourceView );
   }

   public void update( Observable observable, Object o )
   {
      textArea.setText( model.getIndex().note().get() );
   }
}
