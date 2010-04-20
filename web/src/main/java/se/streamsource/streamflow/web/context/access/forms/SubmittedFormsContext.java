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

package se.streamsource.streamflow.web.context.access.forms;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormsContext.Mixin.class)
public interface SubmittedFormsContext
   extends Interactions, IndexInteraction<SubmittedFormsListDTO>
{

   abstract class Mixin
      extends InteractionsMixin
      implements SubmittedFormsContext
   {
      public SubmittedFormsListDTO index()
      {
         SubmittedFormsQueries forms = context.get( SubmittedFormsQueries.class );
         return forms.getSubmittedForms();
      }
   }
}