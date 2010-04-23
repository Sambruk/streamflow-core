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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;

import java.util.Collection;

/**
 * JAVADOC
 */
public class CaseActionsModel
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   public Actions actions()
   {
      try
      {
         return client.query( "actions", Actions.class );
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public EventList<TitledLinkValue> getPossibleProjects()
   {
      try
      {
         BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

         LinksValue linksValue = client.query( "possiblesendto", LinksValue.class );
         list.addAll( (Collection) linksValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

   // Actions
   public void open()
   {
      try
      {
         client.putCommand( "open" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void assignToMe()
   {
      try
      {
         client.postCommand( "assign" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void close()
   {
      try
      {
         client.putCommand( "close" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void delete()
   {
      try
      {
         client.delete();
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }


   public void sendTo( EntityReference to)
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( to );
         client.postCommand( "sendto", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void reopen()
   {
      try
      {
         client.postCommand( "reopen" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void unassign()
   {
      try
      {
         client.postCommand( "unassign" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void onHold()
   {
      try
      {
         client.postCommand( "onhold" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void resume()
   {
      try
      {
         client.postCommand( "resume" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }
}
