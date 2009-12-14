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

package se.streamsource.streamflow.web.domain.comment;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.List;

/**
 * Commentable role
 */
@Mixins(Commentable.Mixin.class)
public interface Commentable
{
   public void addComment( CommentValue comment );

   interface Data
   {
      @UseDefaults
      Property<List<CommentValue>> comments();

      void addedComment( DomainEvent event, CommentValue comment );
   }

   abstract class Mixin
         implements Commentable, Data
   {
      @This
      Data state;

      public void addComment( CommentValue comment )
      {
         addedComment( DomainEvent.CREATE, comment );
      }

      public void addedComment( DomainEvent event, CommentValue comment )
      {
         List<CommentValue> comments = state.comments().get();
         comments.add( comment );
         state.comments().set( comments );
      }
   }
}
