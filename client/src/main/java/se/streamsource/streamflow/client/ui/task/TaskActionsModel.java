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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;

/**
 * JAVADOC
 */
public class TaskActionsModel
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

   public EventList<ListItemValue> getPossibleProjects()
   {
      try
      {
         BasicEventList<ListItemValue> list = new BasicEventList<ListItemValue>();

         ListValue listValue = client.query( "possibleprojects", ListValue.class );
         list.addAll( listValue.items().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

   public EventList<ListItemValue> getPossibleUsers()
   {
      try
      {
         BasicEventList<ListItemValue> list = new BasicEventList<ListItemValue>();

         ListValue listValue = client.query( "possibleusers", ListValue.class );
         list.addAll( listValue.items().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

   // Actions
   public void accept()
   {
      try
      {
         client.postCommand( "accept" );
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

   public void complete()
   {
      try
      {
         client.putCommand( "complete" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void delegate(EntityReference to)
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( to );
         client.postCommand( "delegate", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void delete()
   {
      try
      {
         client.deleteCommand();
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }


   public void done()
   {
      try
      {
         client.postCommand( "done" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void drop()
   {
      try
      {
         client.postCommand( "drop" );
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

   public void reactivate()
   {
      try
      {
         client.postCommand( "reactivate" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void reject()
   {
      try
      {
         client.postCommand( "reject" );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void redo()
   {
      try
      {
         client.postCommand( "redo" );
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
}
