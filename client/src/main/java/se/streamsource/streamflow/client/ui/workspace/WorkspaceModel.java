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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;

import javax.swing.tree.DefaultTreeModel;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class WorkspaceModel
      extends DefaultTreeModel
      implements EventListener, EventVisitor
{
   public EventVisitorFilter eventHandlerFilter;
   private SavedSearchesModel savedSearches;

   public WorkspaceModel( @Uses WorkspaceNode node, @Uses SavedSearchesModel savedSearches)
   {
      super( node );
      this.savedSearches = savedSearches;

      eventHandlerFilter = new EventVisitorFilter( this, "joinedProject", "leftProject", "joinedGroup", "leftGroup",
            "createdProject", "removedProject" );
   }

   @Override
   public WorkspaceNode getRoot()
   {
      return (WorkspaceNode) super.getRoot();
   }

   public SavedSearchesModel getSavedSearches()
   {
      return savedSearches;
   }

   public void notifyEvent( DomainEvent event )
   {
      getRoot().notifyEvent( event );

      eventHandlerFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      Logger.getLogger( "workspace" ).info( "Refresh project list" );
      getRoot().getProjectsNode().refresh();
      reload( getRoot().getProjectsNode() );

      return true;
   }
}
