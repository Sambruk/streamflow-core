/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.interaction.profile;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(MessageRecipient.Mixin.class)
public interface MessageRecipient
{
   public enum MessageDeliveryTypes
   {
      email,
      none
   }
   
   public void changeMessageDeliveryType(MessageDeliveryTypes deliveryType);

   interface Data
   {
      @UseDefaults
      Property<MessageDeliveryTypes> delivery();

      public void changedMessageDeliveryType(@Optional DomainEvent event, MessageDeliveryTypes deliveryType);
   }
   
   abstract class Mixin implements MessageRecipient, Data 
   {
      @This
      Data state;
      
      public void changeMessageDeliveryType(MessageDeliveryTypes deliveryType) 
      {
         if (!deliveryType.equals( delivery().get() ))
            changedMessageDeliveryType(null, deliveryType);
      }
      
      public void changedMessageDeliveryType( DomainEvent event, MessageDeliveryTypes deliveryType)
      {
         state.delivery().set(deliveryType);   
      }
   }
}
