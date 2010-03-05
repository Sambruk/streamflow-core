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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

/**
 * List of possible task types for a task.
 */
public class PossibleTaskTypesModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   private BasicEventList<LinkValue> taskTypeList = new BasicEventList<LinkValue>();

   public EventList<LinkValue> getTaskTypeList()
   {
      return taskTypeList;
   }

   public void refresh() throws OperationException
   {
      try
      {
         LinksValue taskTypes = client.query( "possibletasktypes", LinksValue.class );

         taskTypeList.clear();
         taskTypeList.addAll( taskTypes.links().get() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public void changeTaskType( EntityReference taskType )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( taskType );
         client.putCommand( "changetasktype", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_change_type, e );
      }
   }
}
