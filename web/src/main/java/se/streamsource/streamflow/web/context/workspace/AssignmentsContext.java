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
package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.context.util.TableQueryConverter;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountAssignmentsConcern.class)
@Mixins(AssignmentsContext.Mixin.class)
public interface AssignmentsContext
        extends AbstractFilterContext
{
   public Query<Case> cases(TableQuery tableQuery);

   public void createcase();

   abstract class Mixin
           implements AssignmentsContext
   {
      @Structure
      Module module;

      @Service
      SystemDefaultsService systemConfig;

      public Query<Case> cases(TableQuery tableQuery)
      {
         AssignmentsQueries assignments = RoleMap.role(AssignmentsQueries.class);
         Query<Case> query = assignments.assignments(RoleMap.role(Assignee.class), tableQuery.where()).newQuery(module.unitOfWorkFactory().currentUnitOfWork());

         TableQueryConverter tableQueryConverter = module.objectBuilderFactory().newObjectBuilder(TableQueryConverter.class).use(tableQuery).newInstance();
         Query<Case> convertedQuery = tableQueryConverter.convert(query);
         return convertedQuery;
      }

      public void createcase()
      {
         Drafts drafts = RoleMap.role(Drafts.class);
         CaseEntity caze = drafts.createDraft();

         Owner owner = RoleMap.role(Owner.class);
         caze.changeOwner(owner);

         caze.open();

         caze.assignTo(RoleMap.role(Assignee.class));
         caze.setUnread( false );
      }
   }
}