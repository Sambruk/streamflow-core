/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.casetypes;

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.ResourceActionEnabler;
import se.streamsource.streamflow.client.util.SeparatorListCellRenderer;
import se.streamsource.streamflow.client.util.TabbedResourceView;
import se.streamsource.streamflow.client.util.TitledLinkGroupingComparator;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class CaseTypesView
      extends ListDetailView
{
   CaseTypesModel model;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public CaseTypesView( @Service ApplicationContext context,
                         @Uses final CaseTypesModel model)
   {
      this.model = model;

      final ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get("move"), am.get( "rename" ), am.get("showUsages"), am.get("knowledgeBase"), am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            final CaseTypeModel caseTypeModel = (CaseTypeModel) model.newResourceModel(detailLink);

            new ResourceActionEnabler(am.get("knowledgeBase"))
            {
               @Override
               protected ResourceValue getResource()
               {
                  caseTypeModel.refresh();
                  return caseTypeModel.getResourceValue();
               }
            }.refresh();

            TabbedResourceView view = module.objectBuilderFactory().newObjectBuilder(TabbedResourceView.class).use( caseTypeModel ).newInstance();
            return view;
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_casetype_title ) );

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

            @Override
            protected void failed(Throwable throwable)
            {
               super.failed(throwable);    //To change body of overridden methods use File | Settings | File Templates.
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

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

   @Action
   public Task rename()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.rename_casetype_title ) );

      if (!Strings.empty( dialog.name() ))
      {
         final LinkValue item = (LinkValue) list.getSelectedValue();
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.changeDescription( item, dialog.name() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task move()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(model.getPossibleMoveTo(selected)).newInstance();
      dialog.setPreferredSize( new Dimension(200,300) );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_casetype_to ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.moveForm(selected, dialog.getSelectedLink());
            }
         };
      } else
         return null;
   }

   @Action
   public void knowledgeBase() throws URISyntaxException, IOException
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      LinkValue url = model.getKnowledgeBaseLink(selected);

      Desktop.getDesktop().browse(new URI(url.href().get()));
   }

   @Action
   public void showUsages()
   {
      LinkValue item = (LinkValue) list.getSelectedValue();
      SeparatorList<TitledLinkValue> separatorList = new SeparatorList<TitledLinkValue>( model.usages( item ), new TitledLinkGroupingComparator(), 1, 10000);

      JList list = new JList();
      list.setCellRenderer( new SeparatorListCellRenderer( new LinkListCellRenderer() ) );
      list.setModel( new EventListModel<TitledLinkValue>( separatorList ) );

      dialogs.showOkDialog( this, list );

      separatorList.dispose();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      super.notifyTransactions( transactions );
   }
}