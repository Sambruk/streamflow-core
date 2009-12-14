/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.Action;
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
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.PopupMenuTrigger;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

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
      tree.setEditable( true );

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
                     return i18n.icon( Icons.account, i18n.ICON_24 );
                  else if (o instanceof OrganizationalUnitAdministrationNode)
                     return i18n.icon( Icons.organization, i18n.ICON_24 );
                  else if (o instanceof OrganizationAdministrationNode)
                     return i18n.icon( Icons.account, i18n.ICON_24 );
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

      //Organization Popup
      {
         JPopupMenu orgPopup = new JPopupMenu();
         orgPopup.add( am.get( "createOrganizationalUnit" ) );
         orgPopup.add( am.get( "removeOrganizationalUnit" ) );

         tree.addMouseListener( new PopupMenuTrigger( orgPopup )
         {
            @Override
            protected void showPopup( MouseEvent e )
            {

               if (tree.getSelectionPath() != null &&
                     tree.getSelectionPath().getLastPathComponent() instanceof OrganizationAdministrationNode)
               {
                  super.showPopup( e );
               }
            }
         } );
      }

      //OU Popup
      {
         JPopupMenu ouPopup = new JPopupMenu();
         ouPopup.add( am.get( "createOrganizationalUnit" ) );
         ouPopup.add( am.get( "removeOrganizationalUnit" ) );
         ouPopup.add( new JSeparator() );
         ouPopup.add( am.get( "moveOrganizationalUnit" ) );
         ouPopup.add( am.get( "mergeOrganizationalUnit" ) );

         tree.addMouseListener( new PopupMenuTrigger( ouPopup )
         {
            @Override
            protected void showPopup( MouseEvent e )
            {

               if (tree.getSelectionPath() != null &&
                     tree.getSelectionPath().getLastPathComponent() instanceof OrganizationalUnitAdministrationNode)
               {
                  boolean enabled = (tree.getSelectionPath().getParentPath().getLastPathComponent() instanceof OrganizationalUnitAdministrationNode);
                  am.get( "moveOrganizationalUnit" ).setEnabled( enabled );
                  am.get( "mergeOrganizationalUnit" ).setEnabled( enabled );
                  super.showPopup( e );
               }
            }
         } );
      }

      refreshWhenVisible = new RefreshWhenVisible( model, this );
      addRefreshWhenVisible();
      //addAncestorListener(refreshWhenVisible);
   }


   public JTree getTree()
   {
      return tree;
   }

   @Action
   public void createOrganizationalUnit()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();
      if (node instanceof OrganizationalUnitAdministrationNode)
      {
         OrganizationalUnitAdministrationNode orgNode = (OrganizationalUnitAdministrationNode) node;

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

            model.createOrganizationalUnit( orgNode, dialog.name() );

            for (Integer expandedRow : expandedRows)
            {
               tree.expandRow( expandedRow );
            }
            tree.setSelectionRows( selected );
         }
      }
   }

   @Action
   public void removeOrganizationalUnit()
   {
      Object node = tree.getSelectionPath().getLastPathComponent();
      if (node instanceof OrganizationalUnitAdministrationNode)
      {
         OrganizationalUnitAdministrationNode orgNode = (OrganizationalUnitAdministrationNode) node;

         Object parent = orgNode.getParent();
         if (parent instanceof OrganizationalUnitAdministrationNode)
         {
            OrganizationalUnitAdministrationNode orgParent = (OrganizationalUnitAdministrationNode) parent;

            ConfirmationDialog dialog = confirmationDialog.iterator().next();
            dialogs.showOkCancelHelpDialog( this, dialog, text( StreamFlowResources.confirmation ) );
            if (dialog.isConfirmed())
            {
               try
               {
                  orgParent.model().removeOrganizationalUnit( orgNode.ou().entity().get() );
               } catch (OperationException e)
               {
                  ResourceException ex = (ResourceException) e.getCause();
                  if (ex.getStatus().equals( Status.CLIENT_ERROR_CONFLICT ))
                  {
                     dialogs.showOkCancelHelpDialog( this,
                           new JLabel( i18n.text( AdministrationResources.could_not_remove_organisation_with_open_projects ) ) );
                  } else
                     throw e;

               }
               model.refresh();
            }

         }
      }
   }

   @Action
   public void moveOrganizationalUnit()
   {
      OrganizationalUnitAdministrationNode moved =
            (OrganizationalUnitAdministrationNode) tree.getSelectionPath().getLastPathComponent();

      SelectOrganizationalUnitDialog moveAndMergeDialog = obf.newObjectBuilder( SelectOrganizationalUnitDialog.class ).use( model ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), moveAndMergeDialog, i18n.text( AdministrationResources.move_to ) );

      if (moveAndMergeDialog.target() != null
            && !moved.ou().entity().get().equals( moveAndMergeDialog.target() ))
      {
         try
         {
            moved.model().moveOrganizationalUnit( moveAndMergeDialog.target() );
         } catch (OperationException e)
         {
            ResourceException ex = (ResourceException) e.getCause();
            if (ex.getStatus().equals( Status.CLIENT_ERROR_CONFLICT ))
            {
               dialogs.showOkCancelHelpDialog( this,
                     new JLabel( i18n.text( AdministrationResources.could_not_remove_organisation_with_open_projects ) ) );
            } else
               throw e;
         }
      } else
      {
         dialogs.showOkDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( AdministrationResources.could_not_move_organization ) ) );
      }

   }

   @Action
   public void mergeOrganizationalUnit()
   {
      OrganizationalUnitAdministrationNode moved =
            (OrganizationalUnitAdministrationNode) tree.getSelectionPath().getLastPathComponent();

      SelectOrganizationalUnitDialog moveAndMergeDialog = obf.newObjectBuilder( SelectOrganizationalUnitDialog.class ).use( model ).newInstance();

      dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), moveAndMergeDialog, i18n.text( AdministrationResources.merge_to ) );

      if (moveAndMergeDialog.target() != null
            && !moved.ou().entity().get().equals( moveAndMergeDialog.target() ))
      {
         try
         {
            moved.model().mergeOrganizationalUnit( moveAndMergeDialog.target() );
         } catch (OperationException e)
         {
            ResourceException ex = (ResourceException) e.getCause();
            if (ex.getStatus().equals( Status.CLIENT_ERROR_CONFLICT ))
            {
               dialogs.showOkCancelHelpDialog( this,
                     new JLabel( i18n.text( AdministrationResources.could_not_remove_organisation_with_open_projects ) ) );
            } else
               throw e;
         }
      } else
      {
         dialogs.showOkDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( AdministrationResources.could_not_merge_organization ) ) );
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
