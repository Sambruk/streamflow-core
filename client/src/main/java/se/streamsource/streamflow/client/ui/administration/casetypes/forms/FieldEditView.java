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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.ComboBoxFieldValue;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.domain.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.domain.form.SignatureFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class FieldEditView
      extends JPanel
      implements Refreshable
{
   private static final Map<Class<? extends FieldValue>, Class<? extends JComponent>> editors = new HashMap<Class<? extends FieldValue>, Class<? extends JComponent>>( );

   static
   {
      // Remember to add editors here when creating new types
      editors.put(CheckboxesFieldValue.class, FieldEditorCheckboxesFieldValueView.class);
      editors.put(ComboBoxFieldValue.class, FieldEditorComboBoxFieldValueView.class);
      editors.put(CommentFieldValue.class, FieldEditorCommentFieldValueView.class);
      editors.put(DateFieldValue.class, FieldEditorDateFieldValueView.class);
      editors.put(ListBoxFieldValue.class, FieldEditorListBoxFieldValueView.class);
      editors.put(NumberFieldValue.class, FieldEditorNumberFieldValueView.class);
      editors.put(OptionButtonsFieldValue.class, FieldEditorOptionButtonsFieldValueView.class);
      editors.put(OpenSelectionFieldValue.class, FieldEditorOpenSelectionFieldValueView.class);
      editors.put(TextAreaFieldValue.class, FieldEditorTextAreaFieldValueView.class);
      editors.put(TextFieldValue.class, FieldEditorTextFieldValueView.class);
      editors.put( SignatureFieldValue.class, FieldEditorSignatureFieldValueView.class);
   }

   private FieldValueEditModel model;
   private final CommandQueryClient client;
   private final ObjectBuilderFactory obf;

   public FieldEditView( @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      super(new BorderLayout());

      this.client = client;
      this.obf = obf;
      model = obf.newObjectBuilder( FieldValueEditModel.class ).use( client ).newInstance();
      
      refresh();
   }

   public void refresh()
   {
      model.refresh();

      FieldDefinitionValue fieldDefinitionValue = model.getFieldDefinition();
      FieldValue value = fieldDefinitionValue.fieldValue().get();

      Class<? extends FieldValue> fieldValueType = (Class<FieldValue>) value.getClass().getInterfaces()[0];
      add(obf.newObjectBuilder( editors.get( fieldValueType )).use( model, client ).newInstance());

      invalidate();
      repaint(  );
   }
}
