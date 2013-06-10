/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.CHECKBOX;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTFIELD;

import java.awt.BorderLayout;

import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionAdminValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class FieldEditorTextFieldValueView
      extends JScrollPane
{

   private final FieldValueEditModel model;
   private JComboBox datatypeBox;

   public FieldEditorTextFieldValueView( @Service ApplicationContext context,
                                         @Uses FieldValueEditModel model,
                                         @Structure Module module)
   {
      this.model = model;
      JPanel panel = new JPanel( new BorderLayout() );

      JPanel fieldPanel = new JPanel();
      FormLayout formLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu:grow",
            "pref, pref, pref, pref, pref, pref, pref, pref, top:70dlu, pref" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, fieldPanel );
      formBuilder.setBorder( Borders.createEmptyBorder( "4dlu, 4dlu, 4dlu, 4dlu" ) );

      StateBinder fieldDefinitionBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      fieldDefinitionBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FieldDefinitionAdminValue fieldDefinitionTemplate = fieldDefinitionBinder.bindingTemplate( FieldDefinitionAdminValue.class );

      StateBinder fieldValueBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      fieldValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      TextFieldValue fieldValueTemplate = fieldValueBinder.bindingTemplate( TextFieldValue.class );

      formBuilder.append( i18n.text( AdministrationResources.type_label ), new JLabel( i18n.text( AdministrationResources.text ) ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.mandatory ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( CHECKBOX.newField(), fieldDefinitionTemplate.mandatory() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.statistical ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( CHECKBOX.newField(), fieldDefinitionTemplate.statistical() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.width_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldValueBinder.bind( TEXTFIELD.newField(), fieldValueTemplate.width() ) );
      formBuilder.nextLine();

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      EventComboBoxModel<LinkValue> boxModel = new EventComboBoxModel<LinkValue>( model.getPossibleDatatypes() );
      datatypeBox = new JComboBox( boxModel );
      datatypeBox.setRenderer( new LinkListCellRenderer() );
      boxModel.setSelectedItem( model.getSelectedDatatype() );
      new ActionBinder(am).bind("updateDatatype", datatypeBox);
      formBuilder.add( new JLabel( i18n.text( AdministrationResources.datatype_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( datatypeBox );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.regularexpression_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldValueBinder.bind( TEXTFIELD.newField(), fieldValueTemplate.regularExpression() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.name_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( TEXTFIELD.newField(), fieldDefinitionTemplate.description() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.hint_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldValueBinder.bind( TEXTFIELD.newField(), fieldValueTemplate.hint() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.description_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( TEXTAREA.newField(), fieldDefinitionTemplate.note() ) );
      formBuilder.nextLine();

      formBuilder.add( new JLabel( i18n.text( AdministrationResources.field_id_label ) ) );
      formBuilder.nextColumn( 2 );
      formBuilder.add( fieldDefinitionBinder.bind( TEXTFIELD.newField(), fieldDefinitionTemplate.fieldId() ) );

      FieldValueObserver observer = module.objectBuilderFactory().newObjectBuilder(FieldValueObserver.class).use( model ).newInstance();
      fieldValueBinder.addObserver( observer );
      fieldDefinitionBinder.addObserver( observer );

      fieldValueBinder.updateWith( model.getFieldDefinition().fieldValue().get() );
      fieldDefinitionBinder.updateWith( model.getFieldDefinition() );

      panel.add( fieldPanel, BorderLayout.CENTER );

      setViewportView( panel );
   }
   
   @Action
   public void updateDatatype()
   {
      if (model.DATATYPE_NONE.equals( ((LinkValue)datatypeBox.getSelectedItem()).id().get()))
      {
         model.changeDatatype( null );
      } else 
      {
         model.changeDatatype( ((LinkValue)datatypeBox.getSelectedItem()).id().get()  );
      }
   }
}