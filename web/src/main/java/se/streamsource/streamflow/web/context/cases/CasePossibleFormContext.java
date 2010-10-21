/*
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

package se.streamsource.streamflow.web.context.cases;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
public class CasePossibleFormContext
{
   @Structure
   Module module;

   public void create( )
   {
      FormDrafts formSubmissions = RoleMap.role( FormDrafts.class );
      Form form = RoleMap.role( Form.class );

      formSubmissions.createFormSubmission( form );
   }

   public LinkValue formdraft(  )
   {
      Form form = RoleMap.role( Form.class );

      FormDrafts formSubmissions = RoleMap.role( FormDrafts.class );

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
