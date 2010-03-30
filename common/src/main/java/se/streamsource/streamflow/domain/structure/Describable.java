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

package se.streamsource.streamflow.domain.structure;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import static se.streamsource.streamflow.infrastructure.event.DomainEvent.CREATE;

/**
 * Role for maintaining descriptions of entities.
 */
@Mixins(Describable.Mixin.class)
public interface Describable
{
   void changeDescription( @Optional @MaxLength(50) String newDescription );

   String getDescription();

   interface Data
   {
      @UseDefaults @Optional
      Property<String> description();

      void changedDescription( DomainEvent event, String description );
   }

   public abstract class Mixin
         implements Describable, Data
   {
      public static <T extends Describable> T getDescribable( Iterable<T> collection, String desc )
      {
         for (T describable : collection)
         {
            if (((Data) describable).description().get().equals( desc ))
               return describable;
         }

         throw new IllegalArgumentException( desc );
      }

      public void changeDescription( String newDescription )
      {
         if (!newDescription.equals( description().get() ))
            changedDescription( CREATE, newDescription );
      }

      public String getDescription()
      {
         return description().get();
      }

      // State

      public void changedDescription( DomainEvent event, String description )
      {
         description().set( description );
      }
   }
}
