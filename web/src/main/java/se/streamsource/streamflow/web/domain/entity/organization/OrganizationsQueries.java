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

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(OrganizationsQueries.Mixin.class)
public interface OrganizationsQueries
{
   OrganizationEntity getOrganizationByName( String name );

   QueryBuilder<OrganizationEntity> organizations();

   class Mixin
         implements OrganizationsQueries
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Organizations.Data state;

      public OrganizationEntity getOrganizationByName( String name )
      {
         Describable.Data template = templateFor( Describable.Data.class );
         return qbf.newQueryBuilder( OrganizationEntity.class ).
               where( eq( template.description(), name ) ).
               newQuery( uowf.currentUnitOfWork() ).find();
      }

      public QueryBuilder<OrganizationEntity> organizations()
      {
         return qbf.newQueryBuilder( OrganizationEntity.class ).where( eq( templateFor( Removable.Data.class ).removed(), false ) );
      }
   }
}
