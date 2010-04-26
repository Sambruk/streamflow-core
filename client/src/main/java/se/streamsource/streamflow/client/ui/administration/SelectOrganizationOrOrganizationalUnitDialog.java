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
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class SelectOrganizationOrOrganizationalUnitDialog
      extends JPanel
{

   @Service
   DialogService dialogs;

   private JXTree tree;
   private EntityReference target;

   public SelectOrganizationOrOrganizationalUnitDialog( @Service ApplicationContext context,
                                                        @Uses final AdministrationModel model ) throws Exception
   {
      super( new BorderLayout() );
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      tree = new JXTree( model );

      tree.setRootVisible( false );
      tree.setShowsRootHandles( true );

      DefaultTreeRenderer renderer = new DefaultTreeRenderer( new WrappingProvider(
            new IconValue()
            {
               public Icon getIcon( Object o )
               {
                  if (o instanceof AccountAdministrationNode)
                     return i18n.icon( Icons.account, i18n.ICON_24 );
                  else if (o instanceof OrganizationAdministrationNode)
                     return i18n.icon( Icons.account, i18n.ICON_24 );
                  else if (o instanceof OrganizationalUnitAdministrationNode)
                     return i18n.icon( Icons.organization, i18n.ICON_24 );
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
                  else return o.toString();
               }
            },
            false
      ) );
      tree.setCellRenderer( renderer );
      tree.getSelectionModel().addTreeSelectionListener( new SelectionActionEnabler( am.get("execute")) );

      JPanel toolbar = new JPanel();
      toolbar.setBorder( BorderFactory.createEtchedBorder() );

      add( BorderLayout.CENTER, new JScrollPane(tree) );
      this.setPreferredSize( new Dimension( 200, 300 ) );
   }

   @Action
   public void execute()
   {
      Object selected = tree.getSelectionPath().getLastPathComponent();
      if (selected instanceof AccountAdministrationNode)
      {
         dialogs.showOkCancelHelpDialog(
               WindowUtils.findWindow( this ),
               new JLabel( i18n.text( AdministrationResources.selection_not_an_organizational_unit ) ) );
         return;
      }
      if (selected instanceof OrganizationalUnitAdministrationNode)
      {
         target = ((OrganizationalUnitAdministrationNode)selected).ou().entity().get();

      } else if (selected instanceof OrganizationAdministrationNode)
      {
         target = ((OrganizationAdministrationNode)selected).ou().entity().get();
      }

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public EntityReference target()
   {
      return target;
   }
}