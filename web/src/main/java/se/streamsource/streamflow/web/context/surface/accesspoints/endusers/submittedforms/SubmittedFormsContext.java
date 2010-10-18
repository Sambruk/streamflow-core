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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * JAVADOC
 */
@Mixins(SubmittedFormsContext.Mixin.class)
public interface SubmittedFormsContext
   extends Context, IndexContext<SubmittedFormsListDTO>, SubContexts<SubmittedFormContext>
{

   LinksValue printablesubmittedforms();

   abstract class Mixin
      extends ContextMixin
      implements SubmittedFormsContext
   {
      public SubmittedFormsListDTO index()
      {
         SubmittedFormsQueries forms = roleMap.get( SubmittedFormsQueries.class );
         return forms.getSubmittedForms();
      }

      public SubmittedFormContext context( String id ) throws ContextNotFoundException
      {
         SubmittedForms.Data data = roleMap.get( SubmittedForms.Data.class );

         for (SubmittedFormValue value : data.submittedForms().get())
         {

            if ( value.form().get().identity().equals( id ))
            {

               roleMap.set( value );
               return subContext( SubmittedFormContext.class );
            }
         }

         throw new ContextNotFoundException();
      }

      public LinksValue printablesubmittedforms()
      {
         SubmittedForms.Data data = roleMap.get( SubmittedForms.Data.class );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

         for (SubmittedFormValue value : data.submittedForms().get())
         {
            builder.addLink( "SubmittedForm", value.form().get().identity() );
         }

         return builder.newLinks();
      }
   }
}