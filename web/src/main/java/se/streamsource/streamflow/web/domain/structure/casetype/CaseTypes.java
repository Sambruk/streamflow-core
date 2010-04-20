/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(CaseTypes.Mixin.class)
public interface CaseTypes
{
   // Commands
   CaseType createCaseType( String name );

   void addCaseType( CaseType caseType );

   boolean removeCaseType( CaseType caseType );

   void moveCaseType( CaseType caseType, CaseTypes toCaseTypes );

   // Queries
   Query<SelectedCaseTypes> usages( CaseType caseType );

   interface Data
   {
      @Aggregated
      ManyAssociation<CaseType> caseTypes();

      CaseType createdCaseType( DomainEvent event, String id );

      void removedCaseType( DomainEvent event, CaseType caseType );

      void addedCaseType(DomainEvent event, CaseType caseType);
   }

   abstract class Mixin
         implements CaseTypes, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      public CaseType createCaseType( String name )
      {
         CaseType caseType = createdCaseType( DomainEvent.CREATE, idGen.generate( Identity.class ) );
         addedCaseType(DomainEvent.CREATE, caseType );
         caseType.changeDescription( name );

         return caseType;
      }

      public void addCaseType( CaseType caseType )
      {
         addedCaseType(DomainEvent.CREATE, caseType );
      }

      public void moveCaseType( CaseType caseType, CaseTypes toCaseTypes )
      {
         toCaseTypes.addCaseType( caseType );

         removedCaseType( DomainEvent.CREATE, caseType );
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