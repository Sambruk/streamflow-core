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

package se.streamsource.streamflow.web.domain.entity.form;

import org.qi4j.api.Qi4j;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Notable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.form.Datatype;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeUrl;

/**
 * Label definition
 */
@Concerns(DatatypeDefinitionEntity.RemovableConcern.class)
public interface DatatypeDefinitionEntity
      extends DomainEntity,
      DatatypeDefinition,
      Describable.Data,
      Notable.Data,
      Removable.Data,
      DatatypeUrl.Data
{
   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @Structure
      Module module;
      
      @Structure
      Qi4j qi4j;

      @This
      DatatypeDefinition dataDefinition;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this fieldDefinition
         if (removed)
         {
            {
               Datatype.Data usedDataTypes = QueryExpressions.templateFor( Datatype.Data.class );
               Query<Datatype> fieldTypeUsages = module.queryBuilderFactory().newQueryBuilder(Datatype.class).
                     where(QueryExpressions.eq(usedDataTypes.datatype(), qi4j.dereference( dataDefinition))).
                     newQuery(module.unitOfWorkFactory().currentUnitOfWork());

               for (Datatype fieldType : fieldTypeUsages)
               {
                  fieldType.changeDatatype( null );
               }
            }
         }

         return removed;
      }

      public void deleteEntity()
      {
         next.deleteEntity();
      }
   }
}
