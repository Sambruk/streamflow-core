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
import se.streamsource.streamflow.web.domain.structure.form.FieldType;
import se.streamsource.streamflow.web.domain.structure.form.FieldTypeDefinition;

/**
 * Label definition
 */
@Concerns(FieldTypeDefinitionEntity.RemovableConcern.class)
public interface FieldTypeDefinitionEntity
      extends DomainEntity,
      FieldTypeDefinition,
      Describable.Data,
      Notable.Data,
      Removable.Data
{
   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @Structure
      Module module;

      @This
      FieldTypeDefinition fieldDefinition;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this fieldDefinition
         if (removed)
         {
            {
               FieldType.Data usedFieldTypes = QueryExpressions.templateFor( FieldType.Data.class );
               Query<FieldType> fieldTypeUsages = module.queryBuilderFactory().newQueryBuilder(FieldType.class).
                     where(QueryExpressions.eq(usedFieldTypes.fieldType(), fieldDefinition)).
                     newQuery(module.unitOfWorkFactory().currentUnitOfWork());

               for (FieldType fieldType : fieldTypeUsages)
               {
                  fieldType.changeFieldType( null );
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
