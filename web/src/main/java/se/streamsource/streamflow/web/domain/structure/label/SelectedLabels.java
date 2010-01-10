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

package se.streamsource.streamflow.web.domain.structure.label;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedLabels.Mixin.class)
public interface SelectedLabels
{
   void addLabel( Label label );

   void removeLabel( Label label );

   boolean hasLabel( Label label );

   interface Data
   {
      ManyAssociation<Label> selectedLabels();

      void addedLabel( DomainEvent event, Label label );

      void removedLabel( DomainEvent event, Label label );
   }

   abstract class Mixin
         implements SelectedLabels, Data
   {
      public void addLabel( Label label )
      {
         addedLabel( DomainEvent.CREATE, label );
      }

      public void removeLabel( Label label )
      {
         removedLabel( DomainEvent.CREATE, label );
      }

      public boolean hasLabel( Label label )
      {
         return selectedLabels().contains( label );
      }

      public void addedLabel( DomainEvent event, Label label )
      {
         selectedLabels().add( label );
      }

      public void removedLabel( DomainEvent event, Label label )
      {
         selectedLabels().remove( label );
      }
   }
}
