/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.entity.label;

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
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * Label definition
 */
@Concerns(LabelEntity.RemovableConcern.class)
public interface LabelEntity
      extends DomainEntity,
      Label,
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
      Label label;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this label
         if (removed)
         {
            {
               SelectedLabels.Data selectedLabels = QueryExpressions.templateFor( SelectedLabels.Data.class );
               Query<SelectedLabels> labelUsages = module.queryBuilderFactory().newQueryBuilder(SelectedLabels.class).
                     where(QueryExpressions.contains(selectedLabels.selectedLabels(), label)).
                     newQuery(module.unitOfWorkFactory().currentUnitOfWork());

               for (SelectedLabels labelUsage : labelUsages)
               {
                  labelUsage.removeSelectedLabel( label );
               }
            }

            {
               Labelable.Data selectedLabels = QueryExpressions.templateFor( Labelable.Data.class );
               Query<Labelable> labelUsages = module.queryBuilderFactory().newQueryBuilder(Labelable.class).
                     where(QueryExpressions.contains(selectedLabels.labels(), label)).
                     newQuery(module.unitOfWorkFactory().currentUnitOfWork());

               for (Labelable labelUsage : labelUsages)
               {
                  labelUsage.removeLabel( label );
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
