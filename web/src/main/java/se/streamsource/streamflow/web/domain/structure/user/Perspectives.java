/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.PerspectiveDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.user.PerspectiveEntity;

@Mixins(Perspectives.Mixin.class)
public interface Perspectives
{
   public Perspective createPerspective(PerspectiveDTO perspective);

   public void removePerspective(Perspective perspective);

   interface Data
   {
      @Aggregated
      ManyAssociation<Perspective> perspectives();

      Perspective createdPerspective(@Optional DomainEvent event, String id, PerspectiveDTO perspective);

      void removedPerspective(@Optional DomainEvent event, Perspective perspective);
   }

   abstract class Mixin
           implements Perspectives, Data
   {
      @This
      Data state;

      @Service
      IdentityGenerator idgen;

      @Structure
      Module module;

      public Perspective createPerspective(PerspectiveDTO perspective)
      {
         String id = idgen.generate(Identity.class);
         Perspective newPerspective = createdPerspective(null, id, perspective);
         newPerspective.changeDescription(perspective.name().get());
         return newPerspective;
      }

      public Perspective createdPerspective(DomainEvent event, String id, PerspectiveDTO perspectiveDTO)
      {
         EntityBuilder<PerspectiveEntity> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder(PerspectiveEntity.class, id);
         builder.instance().perspective().set(perspectiveDTO);
         Perspective perspective = builder.newInstance();
         state.perspectives().add(perspective);
         return perspective;
      }

      public void removePerspective(Perspective perspective)
      {
         if (state.perspectives().contains(perspective))
         {
            removedPerspective(null, perspective);
         }
      }

      public void removedPerspective(DomainEvent event, Perspective perspective)
      {
         state.perspectives().remove(perspective);
         module.unitOfWorkFactory().currentUnitOfWork().remove(perspective);
      }
   }
}
