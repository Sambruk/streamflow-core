/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
 *
 */
@Mixins(MarkReadTimeout.Mixin.class)
public interface MarkReadTimeout
{
   void changeTimeout( Long timeoutSec );

   interface Data
   {
      @UseDefaults
      Property<Long> timeout();

      public void changedTimeout( @Optional DomainEvent event, Long timeoutSec );
   }
   abstract class Mixin
      implements MarkReadTimeout
   {
      @This
      Data data;
      public void changeTimeout( Long timeoutSec )
      {
         data.changedTimeout( null, timeoutSec );
      }
   }
}
