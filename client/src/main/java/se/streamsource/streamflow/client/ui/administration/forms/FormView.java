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

package se.streamsource.streamflow.client.ui.administration.forms;

import com.jgoodies.forms.factories.*;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

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
   private final CommandQueryClient client;

   public FormView( @Service ApplicationContext context,
                    @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.client = client;
      this.model = obf.newObjectBuilder( FormModel.class ).use(client).newInstance();
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));
      
      ActionMap am = context.getActionMap( this );

      model.addObserver( this );
      textArea = new JTextArea();
      textArea.setLineWrap( true );
      textArea.setWrapStyleWord( true );
      textArea.setEditable( false );
      add( new JScrollPane(textArea), BorderLayout.CENTER );
      add( new JButton( am.get( "edit" ) ), BorderLayout.SOUTH );

      new RefreshWhenShowing(this, model);
   }

   @org.jdesktop.application.Action
   public void edit()
   {
      //FormEditView formEditView = obf.newObjectBuilder( FormEditView.class ).use( client ).newInstance();
      TabbedResourceView resourceView = obf.newObjectBuilder( TabbedResourceView.class ).use( client ).newInstance();

      AdministrationView adminView = (AdministrationView) SwingUtilities.getAncestorOfClass( AdministrationView.class, this );

      adminView.show( resourceView );
   }

   public void update( Observable observable, Object o )
   {
      textArea.setText( model.getNote() );
   }
}
