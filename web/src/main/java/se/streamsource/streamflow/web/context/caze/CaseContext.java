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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Reference;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseValue;
import se.streamsource.streamflow.web.context.conversation.ConversationsContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.dci.api.SubContext;

/**
 * JAVADOC
 */
@Mixins(CaseContext.Mixin.class)
public interface CaseContext
   extends Interactions, CaseActionsContext
{
   CaseValue info();

   @SubContext
   CaseGeneralContext general();

   @SubContext
   ConversationsContext conversations();

   @SubContext
   ContactsContext contacts();

   @SubContext
   CaseFormsContext forms();

   abstract class Mixin
      extends InteractionsMixin
      implements CaseContext
   {
      public static CaseValue caseDTO( CaseEntity aCase, Module module, String basePath )
      {
         ValueBuilder<CaseValue> builder = module.valueBuilderFactory().newValueBuilder( CaseValue.class );

         CaseValue prototype = builder.prototype();

         prototype.id().set( aCase.identity().get() );
         prototype.creationDate().set( aCase.createdOn().get() );
         if (aCase.createdBy().get() != null)
            prototype.createdBy().set( ((Describable) aCase.createdBy().get()).getDescription() );
         if (aCase.caseId().get() != null)
            prototype.caseId().set( aCase.caseId().get() );
         prototype.href().set( basePath+"/cases/"+ aCase.identity().get()+"/" );
         prototype.rel().set( "case" );
         if (aCase.owner().get() != null)
            prototype.owner().set( ((Describable) aCase.owner().get()).getDescription() );
         prototype.status().set( aCase.status().get() );
         prototype.text().set( aCase.description().get() );

         if (aCase.caseType().get() != null)
            prototype.caseType().set( aCase.caseType().get().getDescription() );

         if (aCase.isAssigned())
            prototype.assignedTo().set( ((Describable) aCase.assignedTo().get()).getDescription() );

         // Labels
         LinksBuilder labelsBuilder = new LinksBuilder(module.valueBuilderFactory()).path( "labels" ).command( "delete" );
         for (Label label : aCase.labels())
         {
            labelsBuilder.addDescribable( label );
         }
         prototype.labels().set( labelsBuilder.newLinks() );

         return builder.newInstance();
      }

      public CaseValue info()
      {
         return caseDTO(context.get( CaseEntity.class ), module, context.get( Reference.class ).getBaseRef().getPath());
      }

      public CaseGeneralContext general()
      {
         return subContext( CaseGeneralContext.class );
      }

      public ConversationsContext conversations()
      {
         return subContext( ConversationsContext.class );
      }

      public ContactsContext contacts()
      {
         return subContext( ContactsContext.class );
      }

      public CaseFormsContext forms()
      {
         return subContext( CaseFormsContext.class );
      }
   }
}
