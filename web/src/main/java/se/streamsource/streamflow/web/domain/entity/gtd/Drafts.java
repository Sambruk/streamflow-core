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

package se.streamsource.streamflow.web.domain.entity.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.RoleMixin;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

/**
 * JAVADOC
 */
@Mixins(Drafts.Mixin.class)
public interface Drafts
{
   CaseEntity createDraft();

   interface Data
   {
      CaseEntity createdCase( @Optional DomainEvent event, String id );
   }

   abstract class Mixin
      extends RoleMixin<Data>
      implements Drafts, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idGenerator;

      @This Creator creator;

      public CaseEntity createDraft()
      {
         CaseEntity aCase = data.createdCase( null, idGenerator.generate( Identity.class ) );
         aCase.addContact( vbf.newValue( ContactDTO.class ) );

         return aCase;
      }

      public CaseEntity createdCase( DomainEvent event, String id )
      {
         EntityBuilder<CaseEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( CaseEntity.class, id );
         CreatedOn createdOn = builder.instanceFor( CreatedOn.class );
         createdOn.createdOn().set( event.on().get() );

         createdOn.createdBy().set( creator );

         return builder.newInstance();
      }
   }
}