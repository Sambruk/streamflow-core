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
package se.streamsource.streamflow.client.ui.administration.casesettings;

import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.color.ColorUtil;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.FormValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Observable;
import java.util.Observer;

import static java.awt.RenderingHints.*;
import static java.lang.Integer.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Shows case priority settings per case type.
 */
public class PriorityOnCaseView extends JPanel implements Observer, TransactionListener
{
   @Structure
   Module module;

   private PriorityOnCaseModel model;
   private final ApplicationContext context;

   private JCheckBox visible = new JCheckBox(  );
   private JCheckBox mandatory = new JCheckBox( );
   private JComboBox defaultPriority = new JComboBox(  );

   public PriorityOnCaseView( @Service ApplicationContext context, @Uses PriorityOnCaseModel model )
   {
      this.context = context;
      this.model = model;
      this.model.addObserver( this );

      FormLayout layout = new FormLayout( "150dlu, 2dlu, pref, pref:grow", "pref, pref, pref" );
      setLayout( layout );
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.append( i18n.text( AdministrationResources.casepriority_visible ), visible );

      builder.append( i18n.text( AdministrationResources.casepriority_mandatory ), mandatory );

      builder.append( i18n.text( AdministrationResources.casepriority_default_value ), defaultPriority );
      defaultPriority.setRenderer( new LinkListCellRenderer(){
         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
         {

            final PriorityValue itemValue = (PriorityValue) value;
            String val = itemValue == null ? "" : itemValue.text().get();

            JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEADING, 2, 0 ) );

            panel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
            JLabel label = new JLabel( ){
               @Override
               protected void paintComponent(Graphics g) {
                  Color color = getBackground();
                  if( itemValue != null && itemValue.color().get() != null )
                  {
                     color = new Color( parseInt( itemValue.color().get() ) );
                  }
                  final Color FILL_COLOR = ColorUtil.removeAlpha( color );

                  Graphics2D g2 = (Graphics2D) g.create();

                  try {
                     g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                     g2.setColor(Color.LIGHT_GRAY);
                     final int DIAM = Math.min(getWidth(), getHeight());
                     final int inset = 3;
                     g2.fill(new Ellipse2D.Float(inset, inset, DIAM-2*inset, DIAM-2*inset));
                     g2.setColor(FILL_COLOR);
                     final int border = 1;
                     g2.fill(new Ellipse2D.Float(inset+border, inset+border, DIAM-2*inset-2*border, DIAM-2*inset-2*border));
                  } finally {
                     g2.dispose();
                  }
               }
            };
            label.setPreferredSize( new Dimension( 20, 20 ) );
            panel.add( ( Strings.empty( val ) || "-".equals( val ) ) ? new JLabel( ) : label);
            JLabel text = new JLabel( val );

            panel.add( text );
            panel.doLayout();

            return panel;
         }
      });

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      new ActionBinder( am ).bind( "updateVisibility", visible );
      new ActionBinder( am ).bind( "updateMandate", mandatory );
      new ActionBinder( am ).bind( "priorityDefault", defaultPriority );

      new RefreshWhenShowing( this, model );
   }

   public void update(Observable o, Object arg)
   {
      FormValue prioritySettings = (FormValue) model.getIndex();

      visible.setSelected( Boolean.parseBoolean( prioritySettings.form().get().get( "visible" ) ) );
      mandatory.setSelected( Boolean.parseBoolean( prioritySettings.form().get().get( "mandatory" ) ) );
      mandatory.setEnabled( model.command( "updatemandatory" ) != null );

      EventComboBoxModel comboBoxModel = model.getCasePriorities();
      defaultPriority.setModel( comboBoxModel );
      String selectPriority = prioritySettings.form().get().get( "prioritydefault" );
      if( !"".equals( selectPriority ) )
      {
         // omit first element since priority is always null in the first element
         for(int i = 1; i < comboBoxModel.getSize(); i++)
         {
            PriorityValue priorityValue = ((PriorityValue)comboBoxModel.getElementAt( i ));
            if( priorityValue.id().get().equals( selectPriority ))
            {
               defaultPriority.setSelectedItem( comboBoxModel.getElementAt( i ) );
            }
         }
      }
   }

   @Action
   public Task updateVisibility()
   {
      return new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.changeVisibility( visible.isSelected() );
         }
      };

   }

   @Action
   public Task updateMandate()
   {
     return new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.changeMandate( mandatory.isSelected() );
         }
      };

   }

   @Action
   public Task priorityDefault()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.priorityDefault( ((PriorityValue) defaultPriority.getSelectedItem()).id().get() );
         }
      };

   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if(matches( withUsecases( "updatevisibility" ), transactions ) )
         model.refresh();
   }
}
