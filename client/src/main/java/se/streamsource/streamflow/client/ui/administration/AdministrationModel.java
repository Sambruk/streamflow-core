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
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.infrastructure.application.LinkTree;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

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
      this.client = client;
   }

   public void refresh()
   {
      LinkTree administration = client.query( "index", LinkTree.class );

      DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
      sync(root,client, administration);
      reload( (TreeNode) getRoot() );
   }

   private void sync(DefaultMutableTreeNode node, CommandQueryClient parentClient, LinkTree treeNode)
   {
      LinkValue link = treeNode.link().get();
      CommandQueryClient nodeClient = parentClient.getClient( link );
      ContextItem clientInfo = new ContextItem( "", link.text().get(), link.rel().get(), -1, nodeClient);

      node.setUserObject( clientInfo );

      node.removeAllChildren();
      for (LinkTree childTree : treeNode.children().get())
      {
         DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
         node.add( childNode );
         sync(childNode, parentClient, childTree);
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
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();

      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( name );
         contextItem.getClient().getSubClient("organizationalunits" ).postCommand( "createorganizationalunit", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_create_new_organization, e );
      }
   }

   public void removeOrganizationalUnit( Object node)
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();
      try
      {
         contextItem.getClient().delete();
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
