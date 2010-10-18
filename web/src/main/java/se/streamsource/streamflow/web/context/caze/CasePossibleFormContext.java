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
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
@Mixins(CasePossibleFormContext.Mixin.class)
public interface CasePossibleFormContext
      extends Context
{
   void create( );

   LinkValue formdraft( );

   abstract class Mixin
         extends ContextMixin
         implements CasePossibleFormContext
   {
      public void create( )
      {
         FormDrafts formSubmissions = roleMap.get( FormDrafts.class );
         Form form = roleMap.get( Form.class );

         formSubmissions.createFormSubmission( form );
      }

      public LinkValue formdraft(  )
      {
         Form form = roleMap.get( Form.class );

         FormDrafts formSubmissions = roleMap.get( FormDrafts.class );

         FormDraft formSubmission = formSubmissions.getFormSubmission( form );
         if (formSubmission == null)
            throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);

         ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         builder.prototype().id().set( formSubmission.toString() );
         builder.prototype().text().set(formSubmission.toString());
         builder.prototype().rel().set( "formsubmission" );
         builder.prototype().href().set( "../formdrafts/"+formSubmission.toString()+"/" );
         return builder.newInstance();
      }
   }
}
