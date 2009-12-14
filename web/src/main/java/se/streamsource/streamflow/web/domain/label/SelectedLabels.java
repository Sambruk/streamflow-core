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

package se.streamsource.streamflow.web.domain.label;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(SelectedLabels.Mixin.class)
public interface SelectedLabels
{
   void createLabel( Labels labels, String name );

   void addLabel( Label label );

   void removeLabel( Label label );

   boolean hasLabel( Label label );

   interface Data
   {
      ManyAssociation<Label> selectedLabels();

      ListValue possibleLabels( ManyAssociation<Label> labels );

      void labelAdded( DomainEvent event, Label label );

      void labelRemoved( DomainEvent event, Label label );
   }

   abstract class Mixin
         implements SelectedLabels, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      public void createLabel( Labels labels, String name )
      {
         Label label = labels.createLabel( name );
         addLabel( label );
      }

      public void addLabel( Label label )
      {
         labelAdded( DomainEvent.CREATE, label );
      }

      public void removeLabel( Label label )
      {
         labelRemoved( DomainEvent.CREATE, label );
      }

      public boolean hasLabel( Label label )
      {
         return selectedLabels().contains( label );
      }

      public void labelAdded( DomainEvent event, Label label )
      {
         selectedLabels().add( label );
      }

      public void labelRemoved( DomainEvent event, Label label )
      {
         selectedLabels().remove( label );
      }

      public ListValue possibleLabels( ManyAssociation<Label> labels )
      {
         ListValueBuilder builder = new ListValueBuilder( vbf );

         for (Label label : labels)
         {
            if (!selectedLabels().contains( label ))
            {
               builder.addDescribable( label );
            }
         }

         return builder.newList();
      }
   }
}
