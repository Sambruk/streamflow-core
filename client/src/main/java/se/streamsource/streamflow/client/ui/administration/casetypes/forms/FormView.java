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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.SwingXUtilities;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class FormView
      extends JPanel
      implements Observer
{
   private FormModel model;

   @Structure
   ObjectBuilderFactory obf;
   private JTextArea textArea;

   public FormView( @Service ApplicationContext context,
                    @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( FormModel.class ).use(client).newInstance();
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));
      
      model.refresh();
      ActionMap am = context.getActionMap( this );

      model.addObserver( this );
      textArea = new JTextArea( model.getNote() );
      textArea.setLineWrap( true );
      textArea.setWrapStyleWord( true );
      textArea.setEditable( false );
      add( new JScrollPane(textArea), BorderLayout.CENTER );
      add( new JButton( am.get( "edit" ) ), BorderLayout.SOUTH );

   }

   @org.jdesktop.application.Action
   public void edit()
   {
/* TODO The form+fields+field model has to be cleaned up for this to work
      FieldsModel fieldsModel = model.getFieldsModel();

      FormEditAdminView formEditAdminView = obf.newObjectBuilder( FormEditAdminView.class ).
            use( model, fieldsModel ).newInstance();

      AdministrationView adminView = SwingXUtilities.getAncestor( AdministrationView.class, this );

      adminView.show( formEditAdminView );
*/
   }

   public FormModel getModel()
   {
      return model;
   }

   public void update( Observable observable, Object o )
   {
      textArea.setText( model.getNote() );
   }
}
