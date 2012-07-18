/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.List;

/**
 * List of required signatures of a form
 */
@Mixins(RequiredSignatures.Mixin.class)
public interface RequiredSignatures
{
   void createRequiredSignature( RequiredSignatureValue requiredSignatureValue );
   void updateRequiredSignature( int index, RequiredSignatureValue requiredSignatureValue );
   void removeRequiredSignature(int index);

   interface Data
   {
      @UseDefaults
      Property<List<RequiredSignatureValue>> requiredSignatures();

      void createdRequiredSignature( @Optional DomainEvent event, RequiredSignatureValue requiredSignatureValue );
      void updatedRequiredSignature( @Optional DomainEvent event, int index, RequiredSignatureValue requiredSignatureValue );
      void removedRequiredSignature( @Optional DomainEvent event, int index);
   }

   abstract class Mixin
      implements RequiredSignatures, Data
   {
      public void createRequiredSignature( RequiredSignatureValue requiredSignatureValue )
      {
         createdRequiredSignature( null, requiredSignatureValue );
      }

      public void updateRequiredSignature( int index, RequiredSignatureValue requiredSignatureValue )
      {
         if (index >= 0 && index < requiredSignatures().get().size())
            updatedRequiredSignature( null, index, requiredSignatureValue );
      }

      public void removeRequiredSignature( int index )
      {
         if (index >= 0 && index < requiredSignatures().get().size())
            removedRequiredSignature( null, index );
      }

      public void createdRequiredSignature( DomainEvent event, RequiredSignatureValue requiredSignatureValue )
      {
         List<RequiredSignatureValue> signatureValues = requiredSignatures().get();
         signatureValues.add( requiredSignatureValue );
         requiredSignatures().set( signatureValues );
      }

      public void updatedRequiredSignature( DomainEvent event, int index, RequiredSignatureValue requiredSignatureValue )
      {
         List<RequiredSignatureValue> signatureValues = requiredSignatures().get();
         signatureValues.remove( index );
         signatureValues.add( index, requiredSignatureValue );
         requiredSignatures().set( signatureValues );
      }

      public void removedRequiredSignature( DomainEvent event, int index)
      {
         List<RequiredSignatureValue> signatureValues = requiredSignatures().get();
         signatureValues.remove( index );
         requiredSignatures().set( signatureValues );
      }
   }
}
