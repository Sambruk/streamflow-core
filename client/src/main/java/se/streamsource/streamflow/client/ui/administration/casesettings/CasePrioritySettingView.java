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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.FormValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Shows case priority settings per case type.
 */
public class CasePrioritySettingView extends JPanel implements Observer, TransactionListener
{
   @Structure
   Module module;

   private CasePrioritySettingModel model;
   private final ApplicationContext context;

   private JCheckBox visible = new JCheckBox(  );
   private JCheckBox mandatory = new JCheckBox( );

   public CasePrioritySettingView(@Service ApplicationContext context, @Uses CasePrioritySettingModel model)
   {
      this.context = context;
      this.model = model;
      this.model.addObserver( this );

      FormLayout layout = new FormLayout( "150dlu, 2dlu, 50, 70", "pref, pref" );
      setLayout( layout );
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.append( i18n.text( AdministrationResources.casepriority_visible ), visible );

      builder.append( i18n.text( AdministrationResources.casepriority_mandatory ), mandatory );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      new ActionBinder( am ).bind( "updateCasePriorityVisibility", visible );
      new ActionBinder( am ).bind( "updateCasePriorityMandate", mandatory );

      new RefreshWhenShowing( this, model );
   }

   public void update(Observable o, Object arg)
   {
      FormValue prioritySettings = (FormValue) model.getIndex();

      visible.setSelected( Boolean.parseBoolean( prioritySettings.form().get().get( "visible" ) ) );
      mandatory.setSelected( Boolean.parseBoolean( prioritySettings.form().get().get( "mandatory" ) ) );
      mandatory.setEnabled( model.command( "updatemandatory" ) != null );
   }

   @Action
   public void updateCasePriorityVisibility()
   {
      new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.changeCasePriorityVisibility( visible.isSelected() );
         }
      }.execute();

   }

   @Action
   public void updateCasePriorityMandate()
   {
      new CommandTask(){

         @Override
         protected void command() throws Exception
         {
            model.changeCasePriorityMandate( mandatory.isSelected() );
         }
      }.execute();

   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if(matches( withUsecases( "updatevisibility" ), transactions ) )
         model.refresh();
   }
}