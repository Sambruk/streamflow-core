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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;

/**
 * JAVADOC
 */
@Mixins(FormDraftsContext.Mixin.class)
public interface FormDraftsContext
   extends Context, IndexContext<LinksValue>, SubContexts<FormDraftContext>
{
   FormDraftContext context( String id );

   abstract class Mixin
      extends ContextMixin
      implements FormDraftsContext
   {

      public LinksValue index()
      {
         FormSubmissions.Data data = roleMap.get( FormSubmissions.Data.class );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         for (FormSubmission form : data.formSubmissions())
         {
            builder.addLink( form.getFormSubmission().description().get(), EntityReference.getEntityReference( form ));
         }
         return builder.newLinks();
      }

      public FormDraftContext context( String id )
      {
         FormSubmissions.Data data = roleMap.get( FormSubmissions.Data.class );

         for (FormSubmission formSubmission : data.formSubmissions())
         {
            EntityReference entityReference = EntityReference.getEntityReference( formSubmission );
            if ( entityReference.identity().equals( id ))
            {
               roleMap.set( formSubmission );
               roleMap.set( formSubmission.getFormSubmission() );
               return subContext( FormDraftContext.class );
            }
         }
         throw new ContextNotFoundException();
      }
   }
}