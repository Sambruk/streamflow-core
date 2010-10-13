/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.ListDetailView;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import java.awt.Component;


/**
 * JAVADOC
 */
public class FormSignaturesView
      extends ListDetailView
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialog;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private FormSignaturesModel model;

   public FormSignaturesView( @Service ApplicationContext context,
                              @Uses final CommandQueryClient client,
                              @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( FormSignaturesModel.class ).use( client).newInstance();
      ActionMap am = context.getActionMap( this );

      initMaster( new EventListModel<LinkValue>( model.getFormSignatures()), am.get("create"), new javax.swing.Action[] { am.get("remove") },
            new DetailFactory()
            {
               public Component createDetail( LinkValue detailLink )
               {
                  return obf.newObjectBuilder( FormSignatureView.class ).use( client.getClient( detailLink ) ).newInstance();
               }
            });

      new RefreshWhenVisible(this, model );
   }

   @Action
   public Task create()
   {
      final NameDialog dialog = nameDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_signature_title ) );

      if (Strings.notEmpty( dialog.name() ))
      {

         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.create( dialog.name() );
            }
         };
      }
      return null;
   }

   @Action
   public Task remove()
   {
      final LinkValue selected = getSelectedValue();
      if (selected != null)
      {
         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialog.setRemovalMessage( selected.text().get() );
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            return new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.removeFormSignature( selected );
               }
            };
         }
      }

      return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}