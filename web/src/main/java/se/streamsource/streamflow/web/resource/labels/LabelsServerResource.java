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

package se.streamsource.streamflow.web.resource.labels;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.domain.label.Labels;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{labels}/labels
 * /organizations/{labels}/labels
 */
public class LabelsServerResource
      extends CommandQueryServerResource
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   public ListValue labels()
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "labels" );
      Labels labels = uow.get( Labels.class, id );

      ValueBuilder<ListItemValue> builder = vbf.newValueBuilder( ListItemValue.class );
      ListItemValue prototype = builder.prototype();

      ValueBuilder<ListValue> listBuilder = vbf.newValueBuilder( ListValue.class );
      List<ListItemValue> list = listBuilder.prototype().items().get();
      for (Label label : labels.getLabels())
      {
         prototype.entity().set( EntityReference.getEntityReference( label ) );
         prototype.description().set( label.getDescription() );
         list.add( builder.newInstance() );
      }
      return listBuilder.newInstance();
   }

   public void createlabel( StringDTO name ) throws ResourceException
   {
      String identity = getRequest().getAttributes().get( "labels" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      Labels labels = uow.get( Labels.class, identity );

      labels.createLabel( name.string().get() );
   }
}