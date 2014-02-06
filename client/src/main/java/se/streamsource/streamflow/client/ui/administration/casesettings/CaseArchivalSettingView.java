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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.administration.ArchivalSettingsDTO;
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
public class CaseArchivalSettingView extends JPanel implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private CaseArchivalSettingModel model;
   private final ApplicationContext context;

   private JTextField maxAge = new JTextField( 2 );
   private JComboBox archivalType;

   public CaseArchivalSettingView(@Service ApplicationContext context, @Uses CaseArchivalSettingModel model)
   {
      this.context = context;
      this.model = model;
      model.addObserver( this );

      maxAge.setColumns( 2 );
      FormLayout layout = new FormLayout( "150dlu, 2dlu, 50, 70", "pref, pref" );
      setLayout( layout );
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.append( i18n.text( AdministrationResources.max_age ), maxAge );

      archivalType = new JComboBox( new ArchivalSettingsDTO.ArchivalType[]
      { ArchivalSettingsDTO.ArchivalType.delete, ArchivalSettingsDTO.ArchivalType.export } );

      archivalType.setRenderer( new DefaultListCellRenderer()
      {

         @Override
         public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1)
         {
            ArchivalSettingsDTO.ArchivalType type = (ArchivalSettingsDTO.ArchivalType) o;
            return super.getListCellRendererComponent( jList,
                  i18n.text( AdministrationResources.valueOf( type.toString() ) ), i, b, b1 );
         }
      } );
      builder.append( i18n.text( AdministrationResources.archival_type ), archivalType, 2 );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      new ActionBinder( am ).bind( "updateArchivalSettings", maxAge );
      new ActionBinder( am ).bind( "updateArchivalSettings", archivalType );

      new RefreshWhenShowing( this, model );
   }

   public void update(Observable o, Object arg)
   {
      ArchivalSettingsDTO archivalSettings = (ArchivalSettingsDTO) model.getIndex();
      if (archivalSettings == null)
      {
         maxAge.setText( "0" );
         archivalType.setSelectedItem( ArchivalSettingsDTO.ArchivalType.delete );
      } else
      {
         maxAge.setText( archivalSettings.maxAge().get().toString() );
         archivalType.setSelectedItem( archivalSettings.archivalType().get() );
      }

   }

   @org.jdesktop.application.Action
   public void updateArchivalSettings()
   {
      model.changeArchivalSetting( Integer.parseInt( maxAge.getText() ),
            (ArchivalSettingsDTO.ArchivalType) archivalType.getSelectedItem() );
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      model.refresh();
   }
}
