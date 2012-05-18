/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ActionMap;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.administration.DueOnNotificationSettingsDTO;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class CaseDueOnNotificationView
      extends JPanel
      implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private CaseDueOnNotificationModel model;

   private JTextField threshold = new JTextField(2);
   private JCheckBox active = new JCheckBox("Aktiv");

   public CaseDueOnNotificationView(@Service ApplicationContext context, @Uses CaseDueOnNotificationModel model)
   {
      this.model = model;
      model.addObserver( this );

      threshold.setColumns(2);
      FormLayout layout = new FormLayout( "150dlu, 2dlu, 50, 80", "pref" );
      setLayout(layout);
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this);
      builder.append(i18n.text(AdministrationResources.dueon_notification_threshold), threshold);
      builder.append( i18n.text( AdministrationResources.dueon_notification_active), active,2 );

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      new ActionBinder(am).bind("changeDueOnSettings", threshold);
      new ActionBinder(am).bind("changeDueOnSettings", active);
      

      new RefreshWhenShowing( this, model );
   }

   public void update( Observable o, Object arg )
   {
      DueOnNotificationSettingsDTO settings = (DueOnNotificationSettingsDTO) model.getIndex();
      if (settings == null)
      {
         active.setSelected( false );
         threshold.setText("0");
      } else {
         active.setSelected( settings.active().get() );
         threshold.setText(settings.threshold().get().toString());  
      }
   }

   @org.jdesktop.application.Action
   public void changeDueOnSettings()
   {
      model.changeNotificationSettings( Integer.parseInt( threshold.getText() ), active.isSelected(), null);
   }
   
   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }
}
