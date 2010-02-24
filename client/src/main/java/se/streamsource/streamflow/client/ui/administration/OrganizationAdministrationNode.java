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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * JAVADOC
 */
public class OrganizationAdministrationNode
      extends DefaultMutableTreeNode implements Transferable, EventListener
{
   @Structure
   ObjectBuilderFactory obf;

   OrganizationAdministrationModel model;

   private CommandQueryClient client;

   public OrganizationAdministrationNode( @Uses TreeNode parent, @Uses TreeNodeValue ou, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf ) throws ResourceException
   {
      super( ou.buildWith().prototype() );
      this.client = client;

      CommandQueryClient orgClient = client.getSubClient( ou.entity().get().identity() );
      model = obf.newObjectBuilder( OrganizationAdministrationModel.class )
            .use( orgClient ).newInstance();

      for (TreeNodeValue treeNodeValue : ou.children().get())
      {
         CommandQueryClient ouClient = orgClient.getSubClient("organizationalunits")
               .getSubClient( treeNodeValue.entity().get().identity() );
         add( obf.newObjectBuilder( OrganizationalUnitAdministrationNode.class )
               .use( this, treeNodeValue, ouClient ).newInstance() );
      }
   }

   @Override
   public String toString()
   {
      return ou().description().get();
   }

   public TreeNodeValue ou()
   {
      return (TreeNodeValue) getUserObject();
   }

   @Override
   public void setUserObject( Object userObject )
   {
      model.changeDescription( userObject.toString() );
      ou().description().set( userObject.toString() );
   }

   public OrganizationAdministrationModel model()
   {
      return model;
   }

   public DataFlavor[] getTransferDataFlavors()
   {

      DataFlavor[] result = {new DataFlavor( OrganizationAdministrationNode.class, "OrganizationalStructureNode" )};
      return result;
   }

   public boolean isDataFlavorSupported( DataFlavor dataFlavor )
   {
      return "OrganizationalStructureNode".equals( dataFlavor.getHumanPresentableName() );
   }

   public Object getTransferData( DataFlavor dataFlavor ) throws UnsupportedFlavorException, IOException
   {

      return ((OrganizationAdministrationNode) parent).ou().entity().get();
   }

   public void notifyEvent( DomainEvent event )
   {
      model.notifyEvent( event );

      if (children != null)
      {
         for (Object child : children)
         {
            ((EventListener) child).notifyEvent( event );
         }
      }
   }
}