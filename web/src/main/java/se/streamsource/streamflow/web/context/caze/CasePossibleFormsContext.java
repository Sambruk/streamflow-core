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
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

/**
 * JAVADOC
 */
@Mixins(CasePossibleFormsContext.Mixin.class)
public interface CasePossibleFormsContext
      extends SubContexts<CasePossibleFormContext>, Context, IndexContext<LinksValue>
{
   abstract class Mixin
         extends ContextMixin
         implements CasePossibleFormsContext
   {
      public LinksValue index()
      {
         SelectedForms.Data possibleForms = possibleForms();

         if (possibleForms != null)
         {
            return new LinksBuilder( module.valueBuilderFactory() ).addDescribables( possibleForms.selectedForms() ).newLinks();
         } else
         {
            return new LinksBuilder( module.valueBuilderFactory() ).newLinks();
         }
      }

      private SelectedForms.Data possibleForms()
      {
         SelectedForms.Data forms = null;
         TypedCase.Data typedCase = roleMap.get( TypedCase.Data.class );

         CaseType caseType = typedCase.caseType().get();

         if (caseType != null)
         {
            forms = (SelectedForms.Data) caseType;
         }
         return forms;
      }

      public CasePossibleFormContext context( String id )
      {
         SelectedForms.Data data = possibleForms();

         FormEntity formEntity = module.unitOfWorkFactory().currentUnitOfWork().get( FormEntity.class, id );
         if ( data.selectedForms().contains( formEntity ) )
         {
            roleMap.set( formEntity );
         } else
         {
            throw new ContextNotFoundException();
         }

         return subContext( CasePossibleFormContext.class );
      }
   }
}
