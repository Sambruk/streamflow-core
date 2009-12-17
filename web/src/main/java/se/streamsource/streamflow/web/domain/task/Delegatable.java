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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.project.Members;
import se.streamsource.streamflow.web.domain.user.User;

import java.util.Date;

/**
 * JAVADOC
 */
@Mixins(Delegatable.Mixin.class)
public interface Delegatable
{
   void delegateTo( Delegatee delegatee, Delegator delegator, WaitingFor delegatedFrom );

   void rejectDelegation();

   boolean isDelegated();

   boolean isDelegatedBy(User user);

   boolean isDelegatedTo(User user);

   interface Data
   {
      @Optional
      Association<Delegatee> delegatedTo();

      @Optional
      Association<Delegator> delegatedBy();

      @Optional
      Association<WaitingFor> delegatedFrom();

      @Optional
      Property<Date> delegatedOn();

      void delegatedTo( DomainEvent create, Delegatee delegatee, Delegator delegator, WaitingFor delegatedFrom );

      void rejectedDelegation( DomainEvent event );
   }

   abstract class Mixin
         implements Delegatable, Data
   {
      @This
      Ownable.Data ownable;

      @This
      Task task;

      @Structure
      Qi4j api;

      public void delegateTo( Delegatee delegatee, Delegator delegator, WaitingFor delegatedFrom )
      {
         delegatedTo( DomainEvent.CREATE, delegatee, delegator, delegatedFrom );
      }

      public void delegatedTo( DomainEvent event, Delegatee delegatee, Delegator delegator, WaitingFor delegatedFrom )
      {
         delegatedTo().set( delegatee );
         delegatedBy().set( delegator );
         delegatedOn().set( event.on().get() );
         delegatedFrom().set( delegatedFrom );
      }

      public void rejectDelegation()
      {
         if (delegatedTo().get() != null)
         {
            WaitingFor waitingFor = delegatedFrom().get();
            waitingFor.rejectTask( task );
            rejectedDelegation( DomainEvent.CREATE );
         }
      }

      public boolean isDelegated()
      {
         return delegatedTo().get() != null;
      }

      public boolean isDelegatedBy( User user )
      {
         return user.equals( delegatedBy().get());
      }

      public boolean isDelegatedTo( User user )
      {
         if (delegatedTo().get() != null)
         {
            if (delegatedTo().get() instanceof Members) // Project check
            {
               Members members = (Members) delegatedTo().get();
               return (members.isMember( user ));
            } else
            {
               return delegatedTo().get().equals(user); // User check
            }
         } else
            return false;
      }

      public void rejectedDelegation( DomainEvent event )
      {
         delegatedTo().set( null );
         delegatedBy().set( null );
         delegatedOn().set( null );
         delegatedFrom().set( null );
      }
   }
}