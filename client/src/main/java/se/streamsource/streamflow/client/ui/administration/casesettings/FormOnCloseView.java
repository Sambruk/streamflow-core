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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * View representation what form that has to be submitted before a case of a particular case type can be closed.
 */
public class FormOnCloseView
   extends JPanel
   implements Observer,TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private FormOnCloseModel model;
   private final ApplicationContext context;

   public FormOnCloseView( @Service ApplicationContext context, @Uses FormOnCloseModel model )
   {
      this.context = context;
      this.model = model;
      model.addObserver( this );

      setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

      new RefreshWhenShowing( this, model );
   }

   public void update( Observable o, Object arg )
   {
      removeAll();

      final LinkValue link = model.getIndex();

         JButton comp = new JButton( text(AdministrationResources.choose_form_on_close ) + ": " + link.text().get() );
         comp.addActionListener( new ActionListener()
         {
            public void actionPerformed( ActionEvent e )
            {
               final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(model.getPossibleForms( )).newInstance();
               dialog.setPreferredSize( new Dimension(400,200) );

               dialogs.showOkCancelHelpDialog( FormOnCloseView.this, dialog, i18n.text( AdministrationResources.choose_form_on_close ) );

               if (dialog.getSelectedLink() != null)
               {
                  context.getTaskService().execute( new CommandTask()
                  {
                     @Override
                     public void command()
                        throws Exception
                     {
                        model.changeFormOnClose( dialog.getSelectedLink() );
                     }
                  });
               }
            }
         });
         add( comp );

      revalidate();
      repaint();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }
}
