/**
 *
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

package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Resolvable.Mixin.class)
public interface Resolvable
{
   void resolve( Resolution resolution );

   void unresolve();

   interface Data
   {
      @Optional
      Association<Resolution> resolution();

      void resolved( DomainEvent event, Resolution resolution );

      void unresolved( DomainEvent event );
   }

   abstract class Mixin
         implements Resolvable, Data
   {
      public void resolve( Resolution resolution )
      {
         resolved( DomainEvent.CREATE, resolution );
      }

      public void unresolve( )
      {
         unresolved( DomainEvent.CREATE);
      }

      public void resolved( DomainEvent event, Resolution resolution )
      {
         resolution().set(resolution);
      }

      public void unresolved( DomainEvent event)
      {
         resolution().set(null);
      }
   }
}