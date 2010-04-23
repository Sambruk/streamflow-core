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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * JAVADOC
 */
@Mixins(Delegatable.Mixin.class)
public interface Delegatable
{
   void delegateTo( Delegatee delegatee, Delegator delegator, Owner delegatedFrom );

   void rejectDelegation();

   void cancelDelegation();

   boolean isDelegated();

   boolean isDelegatedBy(Delegator delegator);

   boolean isDelegatedTo(Delegatee delegatee);

   interface Data
   {
      @Optional
      Association<Delegatee> delegatedTo();

      @Optional
      Association<Delegator> delegatedBy();

      @Optional
      Association<Owner> delegatedFrom();

      @Optional
      Property<Date> delegatedOn();

      void delegatedTo( DomainEvent create, Delegatee delegatee, Delegator delegator, Owner delegatedFrom );

      void rejectedDelegation( DomainEvent event );

      void cancelledDelegation( DomainEvent event );
   }

   abstract class Mixin
         implements Delegatable, Data
   {
      @This
      Ownable ownable;

      @This
      Status status;

      public void delegateTo( Delegatee delegatee, Delegator delegator, Owner delegatedFrom )
      {
         delegatedTo( DomainEvent.CREATE, delegatee, delegator, delegatedFrom );
      }

      public void delegatedTo( DomainEvent event, Delegatee delegatee, Delegator delegator, Owner delegatedFrom )
      {
         delegatedTo().set( delegatee );
         delegatedBy().set( delegator );
         delegatedOn().set( event.on().get() );
         delegatedFrom().set( delegatedFrom );
//         status.delegate();
      }

      public void rejectDelegation()
      {
         if (delegatedTo().get() != null)
         {
            ownable.sendTo( delegatedFrom().get() );
            rejectedDelegation( DomainEvent.CREATE );
         }
      }

      public void cancelDelegation()
      {
         if (delegatedTo().get() != null)
         {
            ownable.sendTo( delegatedFrom().get() );
            cancelledDelegation( DomainEvent.CREATE );
         }
      }

      public boolean isDelegated()
      {
         return delegatedTo().get() != null;
      }

      public boolean isDelegatedBy( Delegator delegator )
      {
         return delegator.equals( delegatedBy().get());
      }

      public boolean isDelegatedTo( Delegatee delegatee )
      {
         Delegatee delegatedTo = delegatedTo().get();

         if (delegatedTo != null)
         {
            return delegatedTo.isDelegatedTo( delegatee );
         } else
            return false;
      }

      public void rejectedDelegation( DomainEvent event )
      {
         delegatedTo().set( null );
         delegatedBy().set( null );
         delegatedOn().set( null );
         delegatedFrom().set( null );
         status.reopen();
      }

      public void cancelledDelegation( DomainEvent event )
      {
         delegatedTo().set( null );
         delegatedBy().set( null );
         delegatedOn().set( null );
         delegatedFrom().set( null );
      }
   }
}