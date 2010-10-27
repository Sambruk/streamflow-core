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

package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.util.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.util.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

/**
 * JAVADOC
 */
public class AdministrationTreeView
      extends JPanel
      implements TransactionListener
{
   private JXTree tree;

   @Service
   DialogService dialogs;
   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   private AdministrationModel model;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   public AdministrationTreeView( @Service ApplicationContext context,
                                  @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf ) throws Exception
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( AdministrationModel.class ).use( client ).newInstance();
      tree = new JXTree( model );

      tree.setRootVisible( true );
      tree.setShowsRootHandles( true );

      DefaultTreeRenderer renderer = new DefaultTreeRenderer( new WrappingProvider(
            new IconValue()
            {
               public Icon getIcon( Object o )
               {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                  ContextItem clientInfo = (ContextItem) node.getUserObject();
                  if (clientInfo == null)
                     return i18n.icon( Icons.server );
                  else
                     return i18n.icon( Icons.valueOf( clientInfo.getRelation() ) );
               }
            },
            new StringValue()
            {
               public String getString( Object o )
               {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                  ContextItem clientInfo = (ContextItem) node.getUserObject();
                  if (clientInfo == null)
                     return "...";
                  else
                     return clientInfo.getName();
               }
            },
            false
      ) );
      tree.setCellRenderer( renderer );

      JPanel toolbar = new JPanel();
      toolbar.setBorder( BorderFactory.createEtchedBorder() );

      add( new JScrollPane( tree ), BorderLayout.CENTER );

      final ActionMap am = context.getActionMap( this );

      JPopupMenu adminPopup = new JPopupMenu();
      adminPopup.add( am.get( "changeDescription" ) );
      adminPopup.add( am.get( "delete" ) );
      adminPopup.add( new JSeparator() );
      adminPopup.add( am.get( "move" ) );
      adminPopup.add( am.get( "merge" ) );

      JPanel actions = new JPanel();
      actions.add( new JButton( am.get( "createOrganizationalUnit" ) ) );
      actions.add( new JButton( new OptionsAction( adminPopup ) ) );

      add( actions, BorderLayout.SOUTH );

      new RefreshWhenVisible( this, model );

      tree.getSelectionModel().addTreeSelectionListener( new SelectionActionEnabler(
            am.get( "changeDescription" ),
            am.get( "delete" ),
            am.get( "move" ),
            am.get( "merge" ),
            am.get( "createOrganizationalUnit" ) )
      {
         private List<String> commands = new ArrayList<String>();

         @Override
         protected void selectionChanged()
         {
            commands.clear();
            ContextItem contextItem = (ContextItem) ((DefaultMutableTreeNode) (tree.getSelectionPath().getLastPathComponent())).getUserObject();
            CommandQueryClient client = contextItem.getClient();
            commands.addAll( client.query( "", ResourceValue.class ).commands().get() );
         }

         @Override
         public boolean isSelectedValueValid( javax.swing.Action action )
         {
            String actionName = ((ApplicationAction) action).getName().toLowerCase();
            boolean valid = commands.contains( actionName );
            return valid;
         }
      } );

   }


   public JTree getTree()
   {
      return tree;
   }

   @Action
   public Task changeDescription()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();

      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.change_ou_title ) );
      if (Strings.notEmpty( dialog.name() ))
      {
         if (node instanceof MutableTreeNode)
         {
            DefaultMutableTreeNode orgNode = (DefaultMutableTreeNode) node;
            final ContextItem client = (ContextItem) orgNode.getUserObject();
            final ValueBuilder<se.streamsource.dci.value.StringValue> builder = vbf.newValueBuilder( se.streamsource.dci.value.StringValue.class );
            builder.prototype().string().set( dialog.name() );
            return new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  client.getClient().putCommand( "changedescription", builder.newInstance() );
               }
            };
         }
      }

      return null;
   }

   @Action
   public Task createOrganizationalUnit()
   {
      final Object node = tree.getSelectionPath().getLastPathComponent();

      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_ou_title ) );
      if (Strings.notEmpty( dialog.name() ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.createOrganizationalUnit( node, dialog.name() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task delete()
   {
      final Object node = tree.getSelectionPath().getLastPathComponent();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
      String name = ((ContextItem) mutableTreeNode.getUserObject()).getName();

      dialog.setRemovalMessage( name );
      dialogs.showOkCancelHelpDialog( this, dialog, text( StreamflowResources.confirmation ) );

      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.removeOrganizationalUnit( node );
            }
         };
      } else
         return null;
   }

   @Action
   public Task move()
   {
      EventList<LinkValue> targets = model.possibleMoveTo( tree.getSelectionPath().getLastPathComponent() );
      final SelectLinkDialog listDialog = obf.newObjectBuilder( SelectLinkDialog.class ).use( targets ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), listDialog, i18n.text( AdministrationResources.move_to ) );
      if (listDialog.getSelected() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.move(tree.getSelectionPath().getLastPathComponent(), listDialog.getSelected());
            }
         };
      } else
         return null;
   }

   @Action
   public Task merge()
   {
      EventList<LinkValue> targets = model.possibleMergeWith( tree.getSelectionPath().getLastPathComponent() );
      final SelectLinkDialog listDialog = obf.newObjectBuilder( SelectLinkDialog.class ).use( targets ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), listDialog, i18n.text( AdministrationResources.merge_to ) );
      if (listDialog.getSelected() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.move(tree.getSelectionPath().getLastPathComponent(), listDialog.getSelected());
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames( "changedDescription", "removedOrganizationalUnit", "addedOrganizationalUnit" ), transactions ))
      {
         ArrayList<Integer> expandedRows = new ArrayList<Integer>();
         for (int i = 0; i < tree.getRowCount(); i++)
         {
            if (tree.isExpanded( i ))
               expandedRows.add( i );
         }
         int[] selected = tree.getSelectionRows();

         model.notifyTransactions( transactions );

         for (Integer expandedRow : expandedRows)
         {
            tree.expandRow( expandedRow );
         }

         tree.setSelectionRows( selected );
      }
   }
}
