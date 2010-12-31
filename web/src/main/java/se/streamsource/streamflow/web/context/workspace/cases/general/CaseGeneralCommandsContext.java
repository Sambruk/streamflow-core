/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.workspace.cases.general;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;

import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.DRAFT;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.OPEN;

/**
 * Commands for the General view of a Case. They all require the "write" permission
 */
@Mixins(CaseGeneralCommandsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
@RequiresPermission(PermissionType.write)
public interface CaseGeneralCommandsContext
      extends
      Context
{
   @RequiresStatus({DRAFT, OPEN})
   void changedueon( DateDTO dueOnValue );

   @RequiresStatus({DRAFT, OPEN})
   void casetype( EntityValue dto );

   @RequiresStatus({DRAFT, OPEN})
   void changedescription( @MaxLength(50) StringValue stringValue );

   @RequiresStatus({DRAFT, OPEN})
   void changenote( StringValue noteValue );

   LinksValue possiblecasetypes();

   abstract class Mixin
         implements CaseGeneralCommandsContext
   {
      @Structure
      Module module;

      public void changedescription( StringValue stringValue )
      {
         Describable describable = RoleMap.role( Describable.class );
         describable.changeDescription( stringValue.string().get() );
      }

      public void changenote( StringValue noteValue )
      {
         Notable notable = RoleMap.role( Notable.class );
         notable.changeNote( noteValue.string().get() );
      }

      public void changedueon( DateDTO dueOnValue )
      {
         DueOn dueOn = RoleMap.role( DueOn.class );
         dueOn.dueOn( dueOnValue.date().get() );
      }

      public LinksValue possiblecasetypes()
      {
         CaseTypeQueries aCase = RoleMap.role( CaseTypeQueries.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "casetype" );

         aCase.possibleCaseTypes( builder );

         return builder.newLinks();
      }

      public void casetype( EntityValue dto )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         TypedCase aCase = RoleMap.role( TypedCase.class );

         String entityReference = dto.entity().get();
         if (entityReference != null)
         {
            CaseType caseType = uow.get( CaseType.class, entityReference );
            aCase.changeCaseType( caseType );
         } else
            aCase.changeCaseType( null );
      }
   }
}
