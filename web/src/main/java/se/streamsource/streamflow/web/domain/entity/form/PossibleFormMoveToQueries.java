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

package se.streamsource.streamflow.web.domain.entity.form;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

/**
 * JAVADOC
 */
@Mixins(PossibleFormMoveToQueries.Mixin.class)
public interface PossibleFormMoveToQueries
{
   public void possibleMoveFormTo( LinksBuilder builder);

   abstract class Mixin
         implements PossibleFormMoveToQueries
   {
      @Structure
      Module module;

      public void possibleMoveFormTo( LinksBuilder builder)
      {
         Query<Forms.Data> forms = module.queryBuilderFactory().newQueryBuilder( Forms.Data.class ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         for (Forms.Data form : forms)
         {
            builder.addDescribable( (Describable) form );
         }
      }
   }
}