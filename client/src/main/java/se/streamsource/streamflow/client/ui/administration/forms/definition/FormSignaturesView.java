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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.first;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.events;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

import java.awt.Component;

import javax.swing.ActionMap;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.util.Strings;
import ca.odell.glazedlists.swing.EventListModel;


/**
 * JAVADOC
 */
public class FormSignaturesView
      extends ListDetailView
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private FormSignaturesModel model;

   public FormSignaturesView( @Service ApplicationContext context,
                              @Uses final FormSignaturesModel model)
   {
      this.model = model;
      ActionMap am = context.getActionMap( this );

      initMaster( new EventListModel<LinkValue>( model.getList() ), am.get( "add" ), new javax.swing.Action[]{am.get( "remove" )},
            new DetailFactory()
            {
               public Component createDetail( LinkValue detailLink )
               {
                  return module.objectBuilderFactory().newObjectBuilder( FormSignatureView.class ).use( model.newResourceModel(detailLink)).newInstance();
               }
            } );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_signature_title ) );

      if (!Strings.empty( dialog.name() ))
      {
         list.clearSelection();
         final ValueBuilder<RequiredSignatureValue> builder = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );
         builder.prototype().name().set( dialog.name() );

         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.create( builder.newInstance() );
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
         ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
         dialog.setRemovalMessage( selected.text().get() );
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            list.clearSelection();
            return new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.remove( selected );
               }
            };
         }
      }

      return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      DomainEvent event = first( filter( withNames( "createdRequiredSignature" ), events( transactions ) ) );
      if (event != null)
      {
         list.setSelectedIndex( list.getModel().getSize() - 1 );
      }

      super.notifyTransactions( transactions );
   }
}