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

package se.streamsource.streamflow.client.ui.administration.casesettings;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

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
public class CaseDefaultDaysToCompleteView
      extends JPanel
      implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private CaseDefaultDaysToCompleteModel model;
   private final ApplicationContext context;

   private JTextField defaultDaysToComplete = new JTextField(2);

   public CaseDefaultDaysToCompleteView(@Service ApplicationContext context, @Uses CaseDefaultDaysToCompleteModel model)
   {
      this.context = context;
      this.model = model;
      model.addObserver( this );

      defaultDaysToComplete.setColumns(2);
      FormLayout layout = new FormLayout( "150dlu, 2dlu, 50", "pref" );
      setLayout(layout);
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this);
      builder.append(i18n.text(AdministrationResources.default_days_to_complete), defaultDaysToComplete);

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      new ActionBinder(am).bind("changeDefaultDaysToComplete", defaultDaysToComplete);

      new RefreshWhenShowing( this, model );
   }

   public void update( Observable o, Object arg )
   {
      defaultDaysToComplete.setText(model.getIndex().form().get().get("defaultdaystocomplete"));
   }

   @org.jdesktop.application.Action
   public void changeDefaultDaysToComplete()
   {
      model.changeDefaultDaysToComplete(Integer.parseInt(defaultDaysToComplete.getText()));
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }
}
