/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXRadioGroup;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionAdminValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleCondition;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshComponents;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class
      FieldEditView
      extends JPanel
      implements Refreshable, TransactionListener
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
      editors.put( AttachmentFieldValue.class, FieldEditorAttachmentFieldValueView.class);
      editors.put(FieldGroupFieldValue.class, FieldEditorFieldGroupValueView.class );
      editors.put(GeoLocationFieldValue.class, FieldEditorGeoLocationFieldValueView.class );
   }

   private FieldValueEditModel model;
   private final Module module;

   private ValueBinder valueBinder;
   private ActionBinder actionBinder;
   private RefreshComponents refreshComponents;
   private ActionMap am;


   private JPanel rulePanel;
   private JPanel centerPanel;
   private JComboBox ruleFieldIdCombo = new JComboBox(  );
   private JComboBox ruleConditionCombo = new JComboBox( );
   private JRadioButton visibleWhenTrue;
   private JRadioButton visibleWhenFalse;

   public FieldEditView( @Service ApplicationContext context, @Uses FieldValueEditModel model, @Structure Module module)
   {
      super(new BorderLayout());

      this.module = module;
      this.model = model;

      refreshComponents = new RefreshComponents();
      am = context.getActionMap( this );

      valueBinder = module.objectBuilderFactory().newObject( ValueBinder.class );
      actionBinder = module.objectBuilderFactory().newObjectBuilder( ActionBinder.class ).use( context.getActionMap( this ) ).newInstance();
      actionBinder.setResourceMap( context.getResourceMap( getClass() ) );

      centerPanel = new JPanel( new BorderLayout() );
      add( centerPanel, BorderLayout.CENTER );

      rulePanel = createVisibilityRulePanel();
      add( rulePanel, BorderLayout.SOUTH );
      refreshComponents.visibleOn( "possiblerulefields", rulePanel );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();

      FieldDefinitionAdminValue fieldDefinitionAdminValue = model.getIndex();
      FieldValue value = fieldDefinitionAdminValue.fieldValue().get();

      Class<? extends FieldValue> fieldValueType = (Class<FieldValue>) value.getClass().getInterfaces()[0];
      centerPanel.removeAll();
      centerPanel.add( module.objectBuilderFactory().newObjectBuilder( editors.get( fieldValueType ) ).use( model ).newInstance(), BorderLayout.CENTER );

      valueBinder.update( model.getIndex().rule().get() );
      if(model.query("possiblerulefields") != null )
      {
         EventList<LinkValue> eventList = model.possibleRuleFields() ;
         ruleFieldIdCombo.setModel( new EventComboBoxModel<LinkValue>( eventList ) );
         ruleFieldIdCombo.setSelectedItem( findLinkValueWithId( eventList, model.getIndex().rule().get().field().get() ) );

         EventList<LinkValue> eventList2 = model.possibleRuleConditions();
         ruleConditionCombo.setModel( new EventComboBoxModel<LinkValue>( eventList2 ) );
         ruleConditionCombo.setSelectedItem( findLinkValueWithId( eventList2, model.getIndex().rule().get().condition().get().name() ) );

         if( model.getIndex().rule().get().visibleWhen().get())
         {
            visibleWhenFalse.setSelected( false );
            visibleWhenTrue.setSelected( true );
         } else
         {
            visibleWhenFalse.setSelected( true );
            visibleWhenTrue.setSelected( false );
         }
      }

      refreshComponents.refresh( model.getResourceValue() );

   }

   private JPanel createVisibilityRulePanel()
   {
      rulePanel = new JPanel( );
      FormLayout ruleFormLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu, 5dlu, 45dlu, 5dlu, 150dlu:grow",
            "pref, pref, pref, pref:grow" );

      DefaultFormBuilder ruleFormBuilder = new DefaultFormBuilder( ruleFormLayout, rulePanel );
      ruleFormBuilder.addSeparator( text( AdministrationResources.visibility_rule ) );
      ruleFormBuilder.setBorder( Borders.createEmptyBorder( "4dlu, 4dlu, 4dlu, 4dlu" ));

      ruleFormBuilder.nextLine();

      ruleFormBuilder.add( new JLabel( text( AdministrationResources.rule_field_id ) ) );
      ruleFormBuilder.nextColumn( 2 );
      ruleFormBuilder.add( valueBinder.bind( "fieldId", actionBinder.bind( "changeRuleFieldId", ruleFieldIdCombo ) ) );
      ruleFieldIdCombo.setRenderer( new LinkListCellRenderer() );

      ruleFormBuilder.nextColumn(2);

      ruleFormBuilder.add( new JLabel( text( AdministrationResources.rule_values ) ) );

      ruleFormBuilder.add( module.objectBuilderFactory().newObjectBuilder(VisibilityRuleValuesView.class).use( model.newVisibilityRuleValuesModel() ).newInstance(),
            new CellConstraints(7, 2, 1, 3, CellConstraints.FILL, CellConstraints.FILL ));

      ruleFormBuilder.nextLine();

      ruleFormBuilder.add( new JLabel( text( AdministrationResources.rule_condition ) ) );
      ruleFormBuilder.nextColumn( 2 );
      ruleFormBuilder.add( valueBinder.bind( "condition", actionBinder.bind( "changeRuleCondition", ruleConditionCombo ) ) );
      ruleConditionCombo.setRenderer( new DefaultListRenderer()
      {
         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
         {
            if (value instanceof LinkValue)
            {
               LinkValue itemValue = (LinkValue) value;
               String val = itemValue == null ? "" : text( VisibilityRuleCondition.valueOf( itemValue.text().get() ) );

               return super.getListCellRendererComponent( list, val, index, isSelected, cellHasFocus );
            } else return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
         }
      } );

      //ruleFormBuilder.nextLine();

      ruleFormBuilder.add( new JLabel( text( AdministrationResources.rule_visible_when ) ),
            new CellConstraints(1, 4, 1, 1, CellConstraints.FILL, CellConstraints.TOP ) );
      //ruleFormBuilder.nextColumn( 2 );

      javax.swing.Action visibilityWhenToTrueAction = am.get( "changeVisibleWhenToTrue" );
      javax.swing.Action visibilityWhenToFalseAction = am.get( "changeVisibleWhenToFalse" );

      JXRadioGroup buttonGroup = new JXRadioGroup(  );
      buttonGroup.setLayoutAxis( BoxLayout.LINE_AXIS );
      visibleWhenTrue = new JRadioButton( visibilityWhenToTrueAction );
      visibleWhenFalse = new JRadioButton( visibilityWhenToFalseAction );
      buttonGroup.add( visibleWhenTrue );
      buttonGroup.add( visibleWhenFalse );

      ruleFormBuilder.add( buttonGroup, new CellConstraints(3, 4, 1, 1, CellConstraints.FILL, CellConstraints.TOP ) );

      return rulePanel;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeRuleFieldId()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeRuleFieldId( (LinkValue)ruleFieldIdCombo.getSelectedItem() );
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeRuleCondition( )
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeRuleCondition( (LinkValue)ruleConditionCombo.getSelectedItem() );
         }
      };
   }

   @Action
   public Task changeVisibleWhenToTrue()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeRuleVisibleWhen( true );

         }
      };
   }

   @Action
   public Task changeVisibleWhenToFalse()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeRuleVisibleWhen( false );
         }
      };
   }

   private LinkValue findLinkValueWithId( EventList<LinkValue> list, String id )
   {
      for( LinkValue link : list )
      {
         if( link.id().get().equals( id ) )
            return link;
      }
      return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if(Events.matches( Events.withNames( "changedRule" ), transactions ))
      {
         refresh();
      }
   }
}
