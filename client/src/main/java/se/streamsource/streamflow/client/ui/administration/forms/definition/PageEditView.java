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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class PageEditView
      extends JScrollPane
      implements Refreshable, TransactionListener
{

   private ValueBinder valueBinder;
   private ActionBinder actionBinder;
   private RefreshComponents refreshComponents;

   private PageEditModel model;

   private JTextField descriptionField = new JTextField( );
   private JComboBox ruleFieldIdCombo = new JComboBox(  );
   private JComboBox ruleConditionCombo = new JComboBox( );
   private JXRadioGroup buttonGroup;
   private JRadioButton visibleWhenTrue;
   private JRadioButton visibleWhenFalse;


   public PageEditView( @Service ApplicationContext context,
                        @Uses PageEditModel model,
                        @Structure Module module)
   {
      this.model = model;
      JPanel panel = new JPanel( new BorderLayout() );
      refreshComponents = new RefreshComponents();

      ActionMap am = context.getActionMap( this );

      valueBinder = module.objectBuilderFactory().newObject( ValueBinder.class );
      actionBinder = module.objectBuilderFactory().newObjectBuilder( ActionBinder.class ).use( context.getActionMap( this ) ).newInstance();
      actionBinder.setResourceMap( context.getResourceMap( getClass() ) );

      JPanel topPanel = new JPanel( new BorderLayout( ) );
      JPanel fieldPanel = new JPanel();
      FormLayout fieldFormLayout = new FormLayout(
            "45dlu, 5dlu, 150dlu:grow",
            "pref, pref" );

      DefaultFormBuilder fieldFormBuilder = new DefaultFormBuilder( fieldFormLayout, fieldPanel );
      fieldFormBuilder.setBorder( Borders.createEmptyBorder( "4dlu, 4dlu, 4dlu, 4dlu" ) );

      fieldFormBuilder.append( text( AdministrationResources.type_label ), new JLabel( text( AdministrationResources.page_break_field_type ) ) );
      fieldFormBuilder.nextLine();

      fieldFormBuilder.add( new JLabel( text( AdministrationResources.name_label ) ) );
      fieldFormBuilder.nextColumn( 2 );
      fieldFormBuilder.add( valueBinder.bind( "description", actionBinder.bind( "changeDescription", descriptionField ) ) );

      topPanel.add( fieldPanel, BorderLayout.NORTH );

      JPanel rulePanel = new JPanel( );
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

      ruleFormBuilder.nextColumn( 2 );

      ruleFormBuilder.add( new JLabel( text( AdministrationResources.rule_values ) ) );

      VisibilityRuleValuesView visibilityRuleValuesView = module.objectBuilderFactory().newObjectBuilder( VisibilityRuleValuesView.class ).use( model.newVisibilityRuleValuesModel() ).newInstance();
      //visibilityRuleValuesView.setMaximumSize( new Dimension(150, 75 ) );
      ruleFormBuilder.add( visibilityRuleValuesView,
            new CellConstraints( 7, 2, 1, 3, CellConstraints.FILL, CellConstraints.FILL ) );

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

      ruleFormBuilder.add( new JLabel( text( AdministrationResources.rule_visible_when ) ),
            new CellConstraints( 1, 4, 1, 1, CellConstraints.FILL, CellConstraints.TOP ) );

      javax.swing.Action visibilityWhenToTrueAction = am.get( "changeVisibleWhenToTrue" );
      javax.swing.Action visibilityWhenToFalseAction = am.get( "changeVisibleWhenToFalse" );

      buttonGroup = new JXRadioGroup(  );
      buttonGroup.setLayoutAxis( BoxLayout.LINE_AXIS );
      visibleWhenTrue = new JRadioButton( visibilityWhenToTrueAction );
      visibleWhenFalse = new JRadioButton( visibilityWhenToFalseAction );
      buttonGroup.add( visibleWhenTrue );
      buttonGroup.add( visibleWhenFalse );

      ruleFormBuilder.add( buttonGroup, new CellConstraints(3, 4, 1, 1, CellConstraints.FILL, CellConstraints.TOP ) );

      topPanel.add( rulePanel, BorderLayout.CENTER );
      panel.add( topPanel, BorderLayout.NORTH );
      panel.add(  new JPanel( ), BorderLayout.CENTER );

      refreshComponents.visibleOn( "possiblerulefields", rulePanel );

      setViewportView( panel );

      new RefreshWhenShowing( this, this );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDescription()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.changeDescription( descriptionField.getText() );
         }
      };
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
   public void refresh()
   {
      model.refresh();
      valueBinder.update( model.getIndex() );
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
      if(Events.matches(Events.withNames( "changedRule" ), transactions ))
      {
         refresh();
         this.revalidate();
      }
   }
}