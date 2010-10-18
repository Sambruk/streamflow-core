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
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;

/**
 * JAVADOC
 */
@Mixins(CaseSubmittedFormsContext.Mixin.class)
public interface CaseSubmittedFormsContext
      extends Context, IndexContext<SubmittedFormsListDTO>
{
   EffectiveFieldsDTO effectivefields();

   SubmittedFormDTO submittedform( IntegerDTO index );

   abstract class Mixin
         extends ContextMixin
         implements CaseSubmittedFormsContext
   {
      public SubmittedFormsListDTO index()
      {
         SubmittedFormsQueries forms = roleMap.get( SubmittedFormsQueries.class );
         return forms.getSubmittedForms();
      }

      public EffectiveFieldsDTO effectivefields()
      {
         SubmittedFormsQueries fields = roleMap.get( SubmittedFormsQueries.class );

         return fields.effectiveFields();
      }

      public SubmittedFormDTO submittedform( IntegerDTO index )
      {
         SubmittedFormsQueries forms = roleMap.get( SubmittedFormsQueries.class );

         return forms.getSubmittedForm( index.integer().get() );
      }
   }
}
