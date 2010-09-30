/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms;

import org.qi4j.api.mixin.Mixins;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormContext.Mixin.class)
public interface SubmittedFormContext
   extends Context
{

   OutputRepresentation getpdf( );

   abstract class Mixin
      extends ContextMixin
      implements SubmittedFormContext
   {

      public OutputRepresentation getpdf(  )
      {
         SubmittedFormValue value = roleMap.get( SubmittedFormValue.class );

         //module.unitOfWorkFactory().currentUnitOfWork().get( Field.class,  );
         //value.values().get().get( 0 ).field().get().identity();
         return null;
      }
   }
}