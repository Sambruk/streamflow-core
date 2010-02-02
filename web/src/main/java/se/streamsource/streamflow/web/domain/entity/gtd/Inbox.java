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

package se.streamsource.streamflow.web.domain.entity.gtd;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;

/**
 * JAVADOC
 */
@Mixins(Inbox.InboxMixin.class)
public interface Inbox
{
   TaskEntity createTask();

   interface Data
   {
      TaskEntity createdTask( DomainEvent event, String id );
   }

   abstract class InboxMixin
         implements Inbox, Data
   {
      @This
      Owner owner;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idGenerator;

      public TaskEntity createTask()
      {
         TaskEntity task = createdTask( DomainEvent.CREATE, idGenerator.generate( Identity.class ) );
         task.sendTo( owner );
         task.addContact( vbf.newValue( ContactValue.class ) );

         return task;
      }

      public TaskEntity createdTask( DomainEvent event, String id )
      {
         EntityBuilder<TaskEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( TaskEntity.class, id );
         CreatedOn createdOn = builder.instanceFor( CreatedOn.class );
         createdOn.createdOn().set( event.on().get() );
         try
         {
            Creator creator = uowf.currentUnitOfWork().get( Creator.class, event.by().get() );
            createdOn.createdBy().set( creator );
         } catch (NoSuchEntityException e)
         {
            // Ignore
         }
         return builder.newInstance();
      }
   }
}
