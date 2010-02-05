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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.FieldTypes;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.TextField;
import java.awt.Component;

/**
 * A dialog for creating a form field. One must provide a name and a type of the field.
 */
public class FieldCreationDialog
      extends JPanel
{

   FormLayout formLayout = new FormLayout(
         "pref, 4dlu, 150dlu", "" );
   private TextField nameField;
   private String name;
   private JComboBox box;


   public FieldCreationDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );

      JPanel panel = new JPanel();
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      nameField = new TextField();
      formBuilder.append( i18n.text( AdministrationResources.name_label ), nameField );
      box = new JComboBox( new FieldTypes[] {
            FieldTypes.text,
            FieldTypes.date,
            FieldTypes.number,
            FieldTypes.selection,
            FieldTypes.comment} );

      box.setRenderer( new DefaultListCellRenderer() {

         @Override
         public Component getListCellRendererComponent( JList jList, Object o, int i, boolean b, boolean b1 )
         {
            FieldTypes type = (FieldTypes) o;
            return super.getListCellRendererComponent( jList, i18n.text( AdministrationResources.valueOf( type.toString() ) ), i, b, b1 );
         }
      });
      formBuilder.append( i18n.text( AdministrationResources.field_type_selection ) , box );
      add( panel, BorderLayout.CENTER );
   }

   @Action
   public void execute()
   {
      name = nameField.getText();

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public FieldTypes getFieldType()
   {
      return (FieldTypes) box.getSelectedItem();
   }

   public String name()
   {
      return name;
   }
}