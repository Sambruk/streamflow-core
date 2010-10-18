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
import se.streamsource.dci.api.SubContexts;
import se.streamsource.streamflow.web.domain.entity.form.FormDraftEntity;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
@Mixins(CaseFormDraftsContext.Mixin.class)
public interface CaseFormDraftsContext
      extends SubContexts<CaseFormDraftContext>, Context
{

   abstract class Mixin
         extends ContextMixin
         implements CaseFormDraftsContext
   {
      public CaseFormDraftContext context( String id )
      {
         FormDrafts.Data data = roleMap.get( FormDrafts.Data.class );

         FormDraftEntity formDraftEntity = module.unitOfWorkFactory().currentUnitOfWork().get( FormDraftEntity.class, id );
         if  ( data.formSubmissions().contains( formDraftEntity ) )
         {
            roleMap.set( formDraftEntity );
         } else
         {
            throw new ContextNotFoundException();
         }

         return subContext( CaseFormDraftContext.class );
      }
   }


}
