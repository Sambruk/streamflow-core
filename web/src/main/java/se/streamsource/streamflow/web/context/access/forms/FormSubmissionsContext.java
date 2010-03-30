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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;

/**
 * JAVADOC
 */
@Mixins(FormSubmissionsContext.Mixin.class)
public interface FormSubmissionsContext
   extends Context, IndexContext<LinksValue>, SubContexts<FormSubmissionContext>
{

   FormSubmissionContext context( String id );

   abstract class Mixin
      extends ContextMixin
      implements FormSubmissionsContext
   {
      public LinksValue index()
      {
         FormSubmissions.Data formSubmissions = context.role( FormSubmissions.Data.class );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

         for (FormSubmission formSubmission : formSubmissions.formSubmissions())
         {
            builder.addLink( formSubmission.getFormSubmission().description().get(), EntityReference.getEntityReference( formSubmission ));
         }

         return builder.newLinks();
      }

      public FormSubmissionContext context( String id )
      {
         FormSubmissions.Data formSubmissions = context.role( FormSubmissions.Data.class );

         for ( FormSubmission formSubmission : formSubmissions.formSubmissions() )
         {
            FormSubmissionEntity entity = (FormSubmissionEntity) formSubmission;
            if ( entity.identity().get().equals( id ))
            {
               context.playRoles( formSubmission );
               context.playRoles( formSubmission.getFormSubmission() );
               return subContext( FormSubmissionContext.class );
            }
         }
         return null;
      }
   }
}