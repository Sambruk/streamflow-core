/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;

import javax.swing.tree.DefaultTreeModel;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class AdministrationModel
      extends DefaultTreeModel
      implements Refreshable, EventListener, EventVisitor
{
   @Structure
   ObjectBuilderFactory obf;

   WeakModelMap<Account, AccountAdministrationNode> nodes = new WeakModelMap<Account, AccountAdministrationNode>()
   {
      @Override
      protected AccountAdministrationNode newModel( Account key )
      {
         return obf.newObjectBuilder( AccountAdministrationNode.class ).use( getRoot(), key ).newInstance();
      }
   };

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "removedOrganizationalUnit", "addedOrganizationalUnit" );

   public AdministrationModel( @Uses AdministrationNode root )
   {
      super( root );
   }

   @Override
   public AdministrationNode getRoot()
   {
      return (AdministrationNode) super.getRoot();
   }

   public void refresh()
   {
      getRoot().refresh();
      reload( getRoot() );
   }

   public void createOrganizationalUnit( Object node, String name )
   {
      if (node instanceof OrganizationalUnitAdministrationNode)
      {
         OrganizationalUnitAdministrationNode orgNode = (OrganizationalUnitAdministrationNode) node;
         orgNode.model().createOrganizationalUnit( name );
      } else if (node instanceof OrganizationAdministrationNode)
      {
         OrganizationAdministrationNode orgNode = (OrganizationAdministrationNode) node;
         orgNode.model().createOrganizationalUnit( name );
      }
   }

   public void removeOrganizationalUnit( Object parentNode, EntityReference ou)
   {
      if (parentNode instanceof OrganizationalUnitAdministrationNode)
      {
         OrganizationalUnitAdministrationNode orgNode = (OrganizationalUnitAdministrationNode) parentNode;
         orgNode.model().removeOrganizationalUnit( ou );
      } else if (parentNode instanceof OrganizationAdministrationNode)
      {
         OrganizationAdministrationNode orgNode = (OrganizationAdministrationNode) parentNode;
         orgNode.model().removeOrganizationalUnit( ou );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      getRoot().notifyEvent( event );

      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      Logger.getLogger( "administration" ).info( "Refresh organizational overview" );
      getRoot().refresh();
      reload( getRoot() );

      return false;
   }
}
