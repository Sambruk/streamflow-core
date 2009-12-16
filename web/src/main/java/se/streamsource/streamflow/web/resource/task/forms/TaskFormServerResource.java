/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.task.forms;

import org.qi4j.api.entity.EntityReference;
import static org.qi4j.api.entity.EntityReference.getEntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.form.Fields;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.domain.tasktype.TypedTask;
import se.streamsource.streamflow.web.domain.tasktype.TaskType;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /tasks/{task}/forms/{form}
 */
public class TaskFormServerResource
      extends CommandQueryServerResource
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   Qi4jSPI spi;

   public TaskFormServerResource()
   {
      setNegotiated( true );
      getVariants().add( new Variant( MediaType.APPLICATION_JSON ) );
   }


   public FormValue form()
   {
      String formId = getRequest().getAttributes().get("form").toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      FormEntity form = uow.get(FormEntity.class, formId);

      ValueBuilder<FormValue> builder = vbf.newValueBuilder(FormValue.class);

      builder.prototype().note().set(form.note().get());
      builder.prototype().description().set(form.description().get());
      builder.prototype().form().set(EntityReference.parseEntityReference(formId));

      return builder.newInstance();
   }


   public ListValue fields() throws ResourceException
   {
      String formId = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();

      Fields.Data fields;
      try
      {
         fields = uow.get( Fields.Data.class, formId );
      } catch (NoSuchEntityException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
      }

      return new ListValueBuilder( vbf ).addDescribableItems( fields.fields() ).newList();
   }


   @Override
   protected String getConditionalIdentityAttribute()
   {
      return "task";
   }
}