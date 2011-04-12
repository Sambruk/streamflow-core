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

package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;

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
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

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
         CaseType caseType = uowf.currentUnitOfWork().newEntity( CaseType.class, id );

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
         Query<SelectedCaseTypes> caseTypeUsages = qbf.newQueryBuilder( SelectedCaseTypes.class ).
               where( QueryExpressions.contains( selectedCaseTypes.selectedCaseTypes(), caseType ) ).
               newQuery( uowf.currentUnitOfWork() );

         return caseTypeUsages;
      }
   }
}