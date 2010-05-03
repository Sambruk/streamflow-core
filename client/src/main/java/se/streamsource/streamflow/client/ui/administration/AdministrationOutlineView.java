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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
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
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * JAVADOC
 */
public class AdministrationOutlineView
      extends JPanel
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
   ObjectBuilderFactory obf;
   private RefreshWhenVisible refreshWhenVisible;

   public AdministrationOutlineView( @Service ApplicationContext context,
                                     @Uses final AdministrationModel model ) throws Exception
   {
      super( new BorderLayout() );
      this.model = model;
      tree = new JXTree( model );

      tree.setRootVisible( false );
      tree.setShowsRootHandles( true );

      if (model.getRoot().accountsModel.organizations().roots().get().isEmpty())
      {
         context.getActionMap().get( "showAdministrationWindow" ).setEnabled( false );
      } else
      {
         context.getActionMap().get( "showAdministrationWindow" ).setEnabled( true );
      }
      DefaultTreeRenderer renderer = new DefaultTreeRenderer( new WrappingProvider(
            new IconValue()
            {
               public Icon getIcon( Object o )
               {
                  if (o instanceof AccountAdministrationNode)
                     return i18n.icon( Icons.account, i18n.ICON_16 );
                  else if (o instanceof OrganizationalUnitAdministrationNode)
                     return i18n.icon( Icons.organizationalUnit, i18n.ICON_16 );
                  else if (o instanceof OrganizationAdministrationNode)
                     return i18n.icon( Icons.organization, i18n.ICON_16 );
                  else
                     return NULL_ICON;
               }
            },
            new StringValue()
            {
               public String getString( Object o )
               {
                  if (o instanceof AdministrationNode)
                     return "                            ";
                  else if (o instanceof AccountAdministrationNode)
                     return ((AccountAdministrationNode) o).accountModel().settings().name().get();
                  else if (o instanceof OrganizationalUnitAdministrationNode)
                     return o.toString();
                  else if (o instanceof OrganizationAdministrationNode)
                     return o.toString();
                  else
                     return "Unknown";
               }
            },
            false
      ) );
      tree.setCellRenderer( renderer );

      JPanel toolbar = new JPanel();
      toolbar.setBorder( BorderFactory.createEtchedBorder() );

      add( BorderLayout.CENTER, tree );

      final ActionMap am = context.getActionMap( this );

      JPopupMenu adminPopup = new JPopupMenu();
      adminPopup.add( am.get( "changeDescription" ) );
      adminPopup.add( am.get( "removeOrganizationalUnit" ) );
      adminPopup.add( new JSeparator() );
      adminPopup.add( am.get( "moveOrganizationalUnit" ) );
      adminPopup.add( am.get( "mergeOrganizationalUnit" ) );

      JPanel actions = new JPanel();
      actions.add(new JButton(am.get( "createOrganizationalUnit" )));
      actions.add(new JButton(new OptionsAction(adminPopup)));

      add( actions, BorderLayout.SOUTH);

      refreshWhenVisible = new RefreshWhenVisible( model, this );
      addRefreshWhenVisible();
      //addAncestorListener(refreshWhenVisible);

      tree.getSelectionModel().addTreeSelectionListener( new SelectionActionEnabler(
            am.get( "changeDescription" ),
            am.get( "removeOrganizationalUnit" ),
            am.get( "moveOrganizationalUnit"),
            am.get( "mergeOrganizationalUnit"),
            am.get( "createOrganizationalUnit"))
      {
         @Override
         public boolean isSelectedValueValid( javax.swing.Action action )
         {
            // TODO This should be done by asking for possible interactions on server instead

            Object node = tree.getLastSelectedPathComponent();

            String name = ((ApplicationAction) action).getName();
            if (asList( "moveOrganizationalUnit","mergeOrganizationalUnit","removeOrganizationalUnit").contains( name ))
            {
               return node instanceof OrganizationalUnitAdministrationNode;
            } else if ("changeDescription".equals( name ))
            {
               return !(node instanceof AccountAdministrationNode);
            } else if ("createOrganizationalUnit".equals( name ))
            {
               return !(node instanceof AccountAdministrationNode);
            }



            return super.isSelectedValueValid( action );
         }
      });

   }


   public JTree getTree()
   {
      return tree;
   }

   @Action
   public void changeDescription()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();

      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_ou_title ) );
      if (dialog.name() != null)
      {
         if (node instanceof MutableTreeNode)
         {
            MutableTreeNode orgNode = (MutableTreeNode) node;
            orgNode.setUserObject( dialog.name() );
            model.refresh();
         }
      }
   }

   @Action
   public void createOrganizationalUnit()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();

      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_ou_title ) );
      if (dialog.name() != null)
      {
         ArrayList<Integer> expandedRows = new ArrayList<Integer>();
         for (int i = 0; i < tree.getRowCount(); i++)
         {
            if (tree.isExpanded( i ))
               expandedRows.add( i );
         }
         int[] selected = tree.getSelectionRows();

         model.createOrganizationalUnit( node, dialog.name() );

         for (Integer expandedRow : expandedRows)
         {
            tree.expandRow( expandedRow );
         }
         tree.setSelectionRows( selected );
      }
   }

   @Action
   public void removeOrganizationalUnit()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();
      if (node instanceof OrganizationalUnitAdministrationNode)
      {
         OrganizationalUnitAdministrationNode orgNode = (OrganizationalUnitAdministrationNode) node;

         ConfirmationDialog dialog = confirmationDialog.iterator().next();
         dialogs.showOkCancelHelpDialog( this, dialog, text( StreamFlowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            ArrayList<Integer> expandedRows = new ArrayList<Integer>();
            for (int i = 0; i < tree.getRowCount(); i++)
            {
               if (tree.isExpanded( i ))
                  expandedRows.add(  i );
            }

            model.removeOrganizationalUnit( orgNode.getParent(), orgNode.ou().entity().get() );

            for (Integer expandedRow : expandedRows)
            {
               tree.expandRow( expandedRow );
            }
         }
      }
   }

   @Action
   public void moveOrganizationalUnit()
   {
      OrganizationalUnitAdministrationNode moved =
            (OrganizationalUnitAdministrationNode) tree.getSelectionPath().getLastPathComponent();

      SelectOrganizationOrOrganizationalUnitDialog moveDialog = obf.newObjectBuilder( SelectOrganizationOrOrganizationalUnitDialog.class ).use( model ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), moveDialog, i18n.text( AdministrationResources.move_to ) );

      if ( moveDialog.target() != null )
      {
         moved.model().moveOrganizationalUnit( moveDialog.target() );
      }

   }

   @Action
   public void mergeOrganizationalUnit()
   {
      OrganizationalUnitAdministrationNode moved =
            (OrganizationalUnitAdministrationNode) tree.getSelectionPath().getLastPathComponent();

      SelectOrganizationalUnitDialog mergeDialog = obf.newObjectBuilder( SelectOrganizationalUnitDialog.class ).use( model ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), mergeDialog, i18n.text( AdministrationResources.merge_to ) );

      if (mergeDialog.target() != null)
      {
         moved.model().mergeOrganizationalUnit( mergeDialog.target() );
      }
   }

   public void removeRefreshWhenVisible()
   {
      removeAncestorListener( refreshWhenVisible );
   }

   public void addRefreshWhenVisible()
   {
      addAncestorListener( refreshWhenVisible );
   }

   @Override
   public void setVisible( boolean aFlag )
   {
      super.setVisible( aFlag );

      if (aFlag)
         model.refresh();
   }
}
