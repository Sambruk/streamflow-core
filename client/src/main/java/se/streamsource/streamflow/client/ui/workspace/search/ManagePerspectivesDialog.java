/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace.search;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.builder.ButtonBarBuilder2;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static org.qi4j.api.specification.Specifications.and;
import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class ManagePerspectivesDialog
        extends JPanel
        implements TransactionListener, Refreshable
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   private PerspectivesModel model;

   private JList perspective;
   private JButton optionButton;

   public ManagePerspectivesDialog(@Service ApplicationContext context, @Structure ObjectBuilderFactory obf, @Uses CommandQueryClient client)
   {
      super(new BorderLayout());
      setBorder(new EmptyBorder(5, 5, 5, 5));
      ActionMap am;
      setActionMap(am = context.getActionMap(this));

      this.model = obf.newObjectBuilder(PerspectivesModel.class).use(client).newInstance();

      perspective = new JList();
      perspective.setCellRenderer(new LinkListCellRenderer());
      perspective.setModel(new EventListModel<LinkValue>(model.getList()));
      JScrollPane scroll = new JScrollPane(perspective);

      add(scroll, BorderLayout.CENTER);

      JPopupMenu options = new JPopupMenu();

      javax.swing.Action removeAction = am.get("remove");
      javax.swing.Action renameAction = am.get("rename");

      options.add(removeAction);
      options.add(renameAction);

      perspective.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(removeAction, renameAction));
      optionButton = new JButton(new OptionsAction(options));

      ButtonBarBuilder2 buttonBuilder = new ButtonBarBuilder2();
      buttonBuilder.addButton(optionButton);
      buttonBuilder.addUnrelatedGap();
      buttonBuilder.addGlue();
      buttonBuilder.addButton(am.get("close"));
      add(buttonBuilder.getPanel(), BorderLayout.SOUTH);
      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task remove()
   {
      if (!perspective.isSelectionEmpty())
      {
         final LinkValue value = (LinkValue) perspective.getSelectedValue();
         return new CommandTask()
         {
            @Override
            public void command()
                    throws Exception
            {
               model.remove(value);
            }
         };
      } else
         return null;
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow(this).dispose();
   }

   @Action
   public Task rename()
   {
      final LinkValue selected = (LinkValue) perspective.getSelectedValue();
      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog(this, dialog, text(WorkspaceResources.change_perspective_title));

      if (!Strings.empty(dialog.name()))
      {
         return new CommandTask()
         {
            @Override
            public void command()
                    throws Exception
            {
               model.changeDescription(selected, dialog.name());
            }
         };
      } else
         return null;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (matches(and(onEntityTypes("se.streamsource.streamflow.web.domain.entity.user.UserEntity",
              "se.streamsource.streamflow.web.domain.entity.user.profile.PerspectiveEntity"),
              withNames("createdPerspective", "changedDescription", "removedPerspective")), transactions))
      {
         refresh();
      }
   }

   public void refresh()
   {
      model.refresh();
   }
}