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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.*;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

/**
 * JAVADOC
 */
public class AdministrationModel
      extends DefaultTreeModel
      implements Refreshable, TransactionListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   private final CommandQueryClient client;

   public AdministrationModel( @Uses CommandQueryClient client)
   {
      super( new DefaultMutableTreeNode() );
      ((MutableTreeNode)getRoot()).setUserObject( new ContextItem("", "Server", Icons.account.name(), -1, client.getClient( "../../../" )));
      this.client = client;
   }

   public void refresh()
   {
      TreeValue organizations = client.query( "organizations", TreeValue.class );

      DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
      sync(root,client.getClient(  "../../../organizations" ), organizations.roots().get() );
      reload( (TreeNode) getRoot() );
   }

   private void sync(DefaultMutableTreeNode node, CommandQueryClient parentClient, Iterable<TreeNodeValue> treeNodevalues)
   {
      node.removeAllChildren();
      for (TreeNodeValue treeNodeValue : treeNodevalues)
      {
         CommandQueryClient childClient = parentClient.getSubClient( treeNodeValue.entity().get().identity() );
         ContextItem clientInfo = new ContextItem( "", treeNodeValue.description().get(), treeNodeValue.nodeType().get(), -1, childClient);
         DefaultMutableTreeNode child = new DefaultMutableTreeNode(clientInfo);
         node.add( child );

         sync(child, childClient.getSubClient( "organizationalunits" ), treeNodeValue.children().get());
      }
   }

   public void changeDescription( Object node, String newDescription )
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem client = (ContextItem) treeNode.getUserObject();

      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newDescription );
      client.getClient().postCommand( "changedescription", builder.newInstance() );
   }

   public void createOrganizationalUnit( Object node, String name )
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem client = (ContextItem) treeNode.getUserObject();

      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( name );
         client.getClient().getSubClient("organizationalunits" ).postCommand( "createorganizationalunit", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_create_new_organization, e );
      }
   }

   public void removeOrganizationalUnit( Object parentNode, EntityReference ou)
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) parentNode;
      ContextItem client = (ContextItem) treeNode.getUserObject();

      try
      {
         client.getClient().getSubClient("organizationalunits").getSubClient( ou.identity() ).delete();
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_remove_organisation_with_open_projects, e );

         } else
         {
            throw new OperationException( AdministrationResources.could_not_remove_organization, e );
         }

      }
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
// TODO       if (Events.matches( transactions, Events.onEntities( )))
      refresh();
   }
}
