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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import java.awt.BorderLayout;
import java.awt.TextField;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.SeparatorListCellRenderer;
import se.streamsource.streamflow.client.util.TitledLinkGroupingComparator;
import se.streamsource.streamflow.client.util.i18n;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

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


   public FieldCreationDialog( @Service ApplicationContext context, @Uses FieldCreationModel model )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );

      JPanel panel = new JPanel();
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      nameField = new TextField();
      formBuilder.append( i18n.text( AdministrationResources.name_label ), nameField );
      box = new JComboBox();
      SeparatorList<TitledLinkValue> separatorList = new SeparatorList<TitledLinkValue>( model.getPossibleFields(), new TitledLinkGroupingComparator(), 1, 10000);
      box.setModel(new EventComboBoxModel<TitledLinkValue>(separatorList));
      box.setRenderer( new SeparatorListCellRenderer(new LinkListCellRenderer()));
      
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

   public LinkValue getAddLink()
   {  
      if (box.getSelectedItem() instanceof LinkValue) 
      {
         return (LinkValue) box.getSelectedItem();
      } 
      return null;
   }

   public String name()
   {
      return name;
   }
}