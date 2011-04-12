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

package se.streamsource.streamflow.client.ui.administration.forms.definition;

import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;


/**
 * JAVADOC
 */
public class FormSignaturesView
      extends ListDetailView
{
   @Service
   DialogService dialogs;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   Iterable<NameDialog> nameDialog;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private FormSignaturesModel model;

   public FormSignaturesView( @Service ApplicationContext context,
                              @Uses final CommandQueryClient client,
                              @Structure final ObjectBuilderFactory obf )
   {
      this.model = obf.newObjectBuilder( FormSignaturesModel.class ).use( client ).newInstance();
      ActionMap am = context.getActionMap( this );

      initMaster( new EventListModel<LinkValue>( model.getList() ), am.get( "add" ), new javax.swing.Action[]{am.get( "remove" )},
            new DetailFactory()
            {
               public Component createDetail( LinkValue detailLink )
               {
                  return obf.newObjectBuilder( FormSignatureView.class ).use( client.getClient( detailLink ) ).newInstance();
               }
            } );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = nameDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_signature_title ) );

      if (!Strings.empty( dialog.name() ))
      {
         list.clearSelection();
         final ValueBuilder<RequiredSignatureValue> builder = vbf.newValueBuilder( RequiredSignatureValue.class );
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
         ConfirmationDialog dialog = confirmationDialog.iterator().next();
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