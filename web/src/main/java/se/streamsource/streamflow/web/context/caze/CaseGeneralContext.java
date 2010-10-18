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

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseGeneralDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.context.structure.labels.LabelableContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;

import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.*;

/**
 * JAVADOC
 */
@Mixins(CaseGeneralContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface CaseGeneralContext
      extends
      IndexContext<CaseGeneralDTO>,
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

   @SubContext
   LabelableContext labels();

   abstract class Mixin
         extends ContextMixin
         implements CaseGeneralContext
   {
      @Structure
      Module module;

      public void changedescription( StringValue stringValue )
      {
         Describable describable = roleMap.get( Describable.class );
         describable.changeDescription( stringValue.string().get() );
      }

      public void changenote( StringValue noteValue )
      {
         Notable notable = roleMap.get( Notable.class );
         notable.changeNote( noteValue.string().get() );
      }

      public CaseGeneralDTO index()
      {
         ValueBuilderFactory vbf = module.valueBuilderFactory();
         ValueBuilder<CaseGeneralDTO> builder = vbf.newValueBuilder( CaseGeneralDTO.class );
         CaseEntity aCase = roleMap.get( CaseEntity.class );
         builder.prototype().description().set( aCase.description().get() );

         CaseType caseType = aCase.caseType().get();
         if (caseType != null)
         {
            ValueBuilder<LinkValue> caseTypeBuilder = vbf.newValueBuilder( LinkValue.class );

            caseTypeBuilder.prototype().text().set( caseType.getDescription() );
            caseTypeBuilder.prototype().id().set( EntityReference.getEntityReference( caseType ).identity() );
            caseTypeBuilder.prototype().href().set( EntityReference.getEntityReference( caseType ).identity() );
            builder.prototype().caseType().set( caseTypeBuilder.newInstance() );
         }

         builder.prototype().note().set( aCase.note().get() );
         builder.prototype().creationDate().set( aCase.createdOn().get() );
         builder.prototype().caseId().set( aCase.caseId().get() );
         builder.prototype().dueOn().set( aCase.dueOn().get() );
         builder.prototype().status().set( aCase.status().get() );

         return builder.newInstance();
      }

      public void changedueon( DateDTO dueOnValue )
      {
         DueOn dueOn = roleMap.get( DueOn.class );
         dueOn.dueOn( dueOnValue.date().get() );
      }

      public LinksValue possiblecasetypes()
      {
         CaseTypeQueries aCase = roleMap.get( CaseTypeQueries.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "casetype" );

         aCase.possibleCaseTypes( builder );

         return builder.newLinks();
      }

      public void casetype( EntityValue dto )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         TypedCase aCase = roleMap.get( TypedCase.class );

         String entityReference = dto.entity().get();
         if (entityReference != null)
         {
            CaseType caseType = uow.get( CaseType.class, entityReference );
            aCase.changeCaseType( caseType );
         } else
            aCase.changeCaseType( null );
      }

      public LabelableContext labels()
      {
         return subContext( LabelableContext.class );
      }
   }
}
