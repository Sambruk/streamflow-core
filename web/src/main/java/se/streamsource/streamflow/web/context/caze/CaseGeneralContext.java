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

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.caze.CaseGeneralDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.context.structure.labels.LabelableContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.dci.api.SubContext;

import static se.streamsource.streamflow.domain.interaction.gtd.States.*;

/**
 * JAVADOC
 */
@Mixins(CaseGeneralContext.Mixin.class)
public interface CaseGeneralContext
   extends
      IndexInteraction<CaseGeneralDTO>,
      Interactions
{
   @RequiresStatus( { ACTIVE, DELEGATED } )
   void changedueon( DateDTO dueOnValue );

   @RequiresStatus( { ACTIVE, DELEGATED } )
   void casetype( EntityReferenceDTO dto );

   @RequiresStatus({ ACTIVE, DELEGATED } )
   void changedescription( StringValue stringValue );

   @RequiresStatus( { ACTIVE, DELEGATED } )
   void changenote( StringValue noteValue );

   LinksValue possiblecasetypes();
   LinksValue possibleforms();

   @SubContext
   LabelableContext labels();

   abstract class Mixin
      extends InteractionsMixin
      implements CaseGeneralContext
   {
      @Structure
      Module module;

      public void changedescription( StringValue stringValue )
      {
         Describable describable = context.get( Describable.class );
         describable.changeDescription( stringValue.string().get() );
      }

      public void changenote( StringValue noteValue )
      {
         Notable notable = context.get( Notable.class );
         notable.changeNote( noteValue.string().get() );
      }

      public CaseGeneralDTO index()
      {
         ValueBuilderFactory vbf = module.valueBuilderFactory();
         ValueBuilder<CaseGeneralDTO> builder = vbf.newValueBuilder( CaseGeneralDTO.class );
         CaseEntity aCase = context.get( CaseEntity.class );
         builder.prototype().description().set( aCase.description().get() );

         ValueBuilder<ListValue> labelsBuilder = vbf.newValueBuilder( ListValue.class );
         ValueBuilder<ListItemValue> labelsItemBuilder = vbf.newValueBuilder( ListItemValue.class );
         for (Label label : aCase.labels())
         {
            labelsItemBuilder.prototype().entity().set( EntityReference.getEntityReference( label ) );
            labelsItemBuilder.prototype().description().set( label.getDescription() );
            labelsBuilder.prototype().items().get().add( labelsItemBuilder.newInstance() );
         }

         CaseType caseType = aCase.caseType().get();
         if (caseType != null)
         {
            ValueBuilder<ListItemValue> caseTypeBuilder = vbf.newValueBuilder( ListItemValue.class );
            caseTypeBuilder.prototype().description().set( caseType.getDescription() );
            caseTypeBuilder.prototype().entity().set( EntityReference.getEntityReference( caseType ) );
            builder.prototype().caseType().set( caseTypeBuilder.newInstance() );
         }

         builder.prototype().labels().set( labelsBuilder.newInstance() );
         builder.prototype().note().set( aCase.note().get() );
         builder.prototype().creationDate().set( aCase.createdOn().get() );
         builder.prototype().caseId().set( aCase.caseId().get() );
         builder.prototype().dueOn().set( aCase.dueOn().get() );
         builder.prototype().status().set( aCase.status().get() );

         return builder.newInstance();
      }

      public void changedueon( DateDTO dueOnValue )
      {
         DueOn dueOn = context.get(DueOn.class);
         dueOn.dueOn( dueOnValue.date().get() );
      }

      public LinksValue possiblecasetypes()
      {
         CaseTypeQueries aCase = context.get( CaseTypeQueries.class);
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "casetype" );

         aCase.caseTypes(builder);

         return builder.newLinks();
      }

      public void casetype( EntityReferenceDTO dto )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         TypedCase aCase = context.get( TypedCase.class);

         EntityReference entityReference = dto.entity().get();
         if (entityReference != null)
         {
            CaseType caseType = uow.get( CaseType.class, entityReference.identity() );
            aCase.changeCaseType( caseType );
         } else
            aCase.changeCaseType( null );
      }

      public LinksValue possibleforms()
      {
         TypedCase.Data typedCase = context.get( TypedCase.Data.class);

         CaseType caseType = typedCase.caseType().get();

         if (caseType != null)
         {
            SelectedForms.Data forms = (SelectedForms.Data) caseType;
            return new LinksBuilder(module.valueBuilderFactory()).addDescribables( forms.selectedForms() ).newLinks();
         } else
         {
            return new LinksBuilder(module.valueBuilderFactory()).newLinks();
         }
      }

      public LabelableContext labels()
      {
         return subContext( LabelableContext.class );
      }
   }
}
