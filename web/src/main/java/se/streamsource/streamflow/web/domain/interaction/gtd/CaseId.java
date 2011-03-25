/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.Matches;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Human readable id
 */
@Mixins(CaseId.Mixin.class)
public interface CaseId
{
   /**
    * Set new id for the case. It needs to be on the format:
    * yyyymmdd-n
    * such as:
    * 20090320-123
    *
    * @param id
    */
   void assignId( @Matches("\\d{8}-\\d*") String id );

   void assignId( IdGenerator idgen );

   interface Data
   {
      @Optional
      Property<String> caseId();


      void assignedCaseId( @Optional DomainEvent event, String id );
   }

   abstract class Mixin
         implements CaseId, Data
   {
      @This
      Data state;

      @This
      CaseId id;

      public void assignId( IdGenerator idgen )
      {
         if (state.caseId().get() == null)
         {
            idgen.assignId( id );
         }
      }

      public void assignId( String id )
      {
         if (state.caseId().get() == null)
         {
            state.assignedCaseId( null, id );
         }
      }

      // Event

      public void assignedCaseId( @Optional DomainEvent event, String id )
      {
         state.caseId().set( id );
      }
   }
}
