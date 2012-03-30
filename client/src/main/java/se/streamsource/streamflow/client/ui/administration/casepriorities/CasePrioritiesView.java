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
package se.streamsource.streamflow.client.ui.administration.casepriorities;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import java.awt.Component;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Case priorities view
 */
public class CasePrioritiesView
      extends ListDetailView
      implements TransactionListener
{
   private CasePrioritiesModel model;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public CasePrioritiesView( @Service ApplicationContext context, @Uses final CasePrioritiesModel model )
   {
      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getUnsortedList() ), am.get( "add" ), new javax.swing.Action[]{am.get( "remove" ), am.get( "up" ), am.get( "down" )}, true, new ListDetailView.DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            return module.objectBuilderFactory().newObjectBuilder( CasePriorityView.class ).use( model.newResourceModel( detailLink ) ).newInstance();
         }
      } );

      new RefreshWhenShowing( this, model );
   }
   
   @Action(block = Task.BlockingScope.COMPONENT)
   public Task add()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_casepriority_title ) );

      if (!Strings.empty( dialog.name() ))
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
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task remove()
   {
      final LinkValue selected = getSelectedValue();

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
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
               model.remove( selected );
            }
         };
      } else
         return null;
   }
   @Action(block = Task.BlockingScope.COMPONENT)
   public Task up()
   {
      final LinkValue selected = getSelectedValue();
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.up( selected );
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task down()
   {
      final LinkValue selected = getSelectedValue();

      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.down( selected );
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      LinkValue oldSelection = getSelectedValue();
      
      super.notifyTransactions( transactions );

      if ( matches( withNames( "createdPriority", "changedPriority", "changedPriorityOrder", "removedPriority" ), transactions ))
      {   
         model.refresh();
         
         if(matches( withNames("createdPriority"),transactions ))
         {
            list.setSelectedIndex( list.getModel().getSize() - 1 );

         } else if( matches( withNames( "changedPriority", "changedPriorityOrder" ), transactions ) )
         {
            int count = 0;
            for( LinkValue link : model.getUnsortedList())
            {
               if( link.text().get().equals( oldSelection.text().get() ))
               {
                  list.setSelectedIndex( count );
                  return;
               }
               count++;
            }
         } else if( matches( withNames( "removedPriority" ), transactions ))
         {
            list.clearSelection();
         }
      }
      
   }
}