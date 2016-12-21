/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(CaseTypes.Mixin.class)
public interface CaseTypes
{
   // Commands
   CaseType createCaseType( String name );

   @ChangesOwner
   void addCaseType( CaseType caseType );

   boolean removeCaseType( CaseType caseType );

   void moveCaseType( CaseType caseType, CaseTypes toCaseTypes );

   void mergeCaseTypes( CaseTypes to );

   // Queries
   Query<SelectedCaseTypes> usages( CaseType caseType );

   interface Data
   {
      @Aggregated
      ManyAssociation<CaseType> caseTypes();

      CaseType createdCaseType( @Optional DomainEvent event, String id );

      void removedCaseType( @Optional DomainEvent event, CaseType caseType );

      void addedCaseType(@Optional DomainEvent event, CaseType caseType);
   }

   abstract class Mixin
         implements CaseTypes, Data
   {
      @This
      Owner owner;

      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Data data;

      public CaseType createCaseType( String name )
      {
         CaseType caseType = createdCaseType( null, idGen.generate( Identity.class ) );
         addCaseType(caseType );
         caseType.changeDescription( name );

         return caseType;
      }

      public void addCaseType( CaseType caseType )
      {
         addedCaseType(null, caseType );
      }

      public void moveCaseType( CaseType caseType, CaseTypes toCaseTypes )
      {
         toCaseTypes.addCaseType( caseType );

         removedCaseType( null, caseType );
      }

      public void mergeCaseTypes( CaseTypes to )
      {
         while (data.caseTypes().count() > 0)
         {
            CaseType caseType = data.caseTypes().get( 0 );
            removedCaseType( null, caseType );
            to.addCaseType( caseType );
         }
      }

      public boolean removeCaseType( CaseType caseType )
      {
         if (data.caseTypes().contains( caseType ))
         {
            removedCaseType( null, caseType );
            caseType.removeEntity();
            return true;
         }

         return true;
      }

      public CaseType createdCaseType( DomainEvent event, String id )
      {
         CaseType caseType = module.unitOfWorkFactory().currentUnitOfWork().newEntity( CaseType.class, id );

         return caseType;
      }

      public void addedCaseType( DomainEvent event, CaseType caseType )
      {
         caseTypes().add( caseType );
      }

      public void removedCaseType( DomainEvent event, CaseType caseType )
      {
         caseTypes().remove( caseType );
      }

      public Query<SelectedCaseTypes> usages( CaseType caseType )
      {
         SelectedCaseTypes.Data selectedCaseTypes = QueryExpressions.templateFor( SelectedCaseTypes.Data.class );
         Query<SelectedCaseTypes> caseTypeUsages = module.queryBuilderFactory().newQueryBuilder( SelectedCaseTypes.class ).
               where( and(
                     eq( templateFor( Removable.Data.class ).removed(), false ),
                     isNotNull( templateFor( Ownable.Data.class ).owner() ),
                     contains( selectedCaseTypes.selectedCaseTypes(), caseType ) )
                     ).
                     newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         return caseTypeUsages;
      }
   }
}