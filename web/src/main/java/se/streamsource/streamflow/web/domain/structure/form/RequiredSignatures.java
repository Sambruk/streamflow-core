/*
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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.List;

/**
 * List of required signatures of a form
 */
public interface RequiredSignatures
{
   void createRequiredSignature(RequiredSignature requiredSignature);
   void updateRequiredSignature( int index, RequiredSignature requiredSignature );
   void removeRequiredSignature(int index);

   interface Data
   {
      Property<List<RequiredSignature>> requiredSignatures();

      void createdRequiredSignature( DomainEvent event, RequiredSignature requiredSignature);
      void updatedRequiredSignature( DomainEvent event, int index, RequiredSignature requiredSignature);
      void removedRequiredSignature( DomainEvent event, int index);
   }

   class Mixin
      implements RequiredSignatures
   {
      @This
      Data data;

      public void createRequiredSignature( RequiredSignature requiredSignature )
      {
         data.createdRequiredSignature( DomainEvent.CREATE, requiredSignature );
      }

      public void updateRequiredSignature( int index, RequiredSignature requiredSignature )
      {
         data.updatedRequiredSignature( DomainEvent.CREATE, index, requiredSignature );
      }

      public void removeRequiredSignature( int index )
      {
         data.removedRequiredSignature( DomainEvent.CREATE, index );
      }
   }
}
