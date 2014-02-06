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
package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

/**
 * JAVADOC
 */
@Mixins(SubCases.Mixin.class)
public interface SubCases
{
   void createSubCase();

   void removeSubCase(Case subCase);

   interface Data
   {
      ManyAssociation<Case> subCases();

      CaseEntity createdSubCase(@Optional DomainEvent event, String id);

      void removedSubCase(@Optional DomainEvent event, Case subCase);
   }

   abstract class Mixin
      implements SubCases, Data
   {
      @Structure
      Module module;

      @Service
      IdentityGenerator idGenerator;

      @This
      Case myself;

      public void createSubCase()
      {
         CaseEntity aCase = createdSubCase( null, idGenerator.generate( Identity.class ) );
         aCase.changeParent( myself );
         aCase.createLog();
         aCase.createNotes();
      }

      public CaseEntity createdSubCase( DomainEvent event, String id )
      {
         EntityBuilder<CaseEntity> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( CaseEntity.class, id );
         CreatedOn createdOn = builder.instanceFor( CreatedOn.class );
         createdOn.createdOn().set( event.on().get() );

         createdOn.createdBy().set( module.unitOfWorkFactory().currentUnitOfWork().get( Creator.class, event.by().get() ));

         CaseEntity caseEntity = builder.newInstance();
         subCases().add(caseEntity  );
         return caseEntity;
      }

      public void removeSubCase( Case subCase )
      {
         if (subCases().contains( subCase ))
         {
            removedSubCase( null, subCase );
            subCase.changeParent( null );
         }
      }

      public void removedSubCase( @Optional DomainEvent event, Case subCase )
      {
         subCases().remove( subCase );
      }
   }
}
