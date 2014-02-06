/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.label;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedLabels.Mixin.class)
public interface SelectedLabels
{
   void addSelectedLabel( Label label );

   void removeSelectedLabel( Label label );

   void mergeSelectedLabels( SelectedLabels labels );

   boolean hasSelectedLabel( Label label );

   interface Data
   {
      ManyAssociation<Label> selectedLabels();

      void addedSelectedLabel( @Optional DomainEvent event, Label label );

      void removedSelectedLabel( @Optional DomainEvent event, Label label );
   }

   class Mixin
         implements SelectedLabels
   {
      @This
      Data data;

      public void addSelectedLabel( Label label )
      {
         data.addedSelectedLabel( null, label );
      }

      public void removeSelectedLabel( Label label )
      {
         data.removedSelectedLabel( null, label );
      }

      public void mergeSelectedLabels( SelectedLabels to )
      {
         while (data.selectedLabels().count() > 0)
         {
            Label label = data.selectedLabels().get( 0 );
            data.removedSelectedLabel( null, label );
            to.addSelectedLabel( label );
         }
      }

      public boolean hasSelectedLabel( Label label )
      {
         return data.selectedLabels().contains( label );
      }
   }
}
