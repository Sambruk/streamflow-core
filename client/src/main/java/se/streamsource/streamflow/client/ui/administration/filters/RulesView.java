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

package se.streamsource.streamflow.client.ui.administration.filters;

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
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * TODO
 */
public class RulesView
   extends ListDetailView
{
   private @Service
   DialogService dialogs;

   private @Structure Module module;

   private RulesModel model;

   public RulesView(@Service ApplicationContext context, @Structure final Module module, @Uses final RulesModel model)
   {
      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            LabelRuleModel labelRuleModel = (LabelRuleModel) model.newResourceModel(detailLink);
            return module.objectBuilderFactory().newObjectBuilder(LabelRuleView.class).use( labelRuleModel).newInstance();
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(model.getPossibleLabels()).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_label_title ) );

      if (dialog.getSelectedLinks() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.createLabelRule(dialog.getSelectedLink());
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      final LinkValue linkValue = model.getIndex().links().get().get(list.getSelectedIndex());
      dialog.setRemovalMessage(linkValue.text().get());
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text(StreamflowResources.confirmation) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.remove(linkValue);
            }
         };
      } else
         return null;
   }

   @Override
   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches(Events.withNames("updatedFilter"), transactions))
         model.refresh();
   }
}
