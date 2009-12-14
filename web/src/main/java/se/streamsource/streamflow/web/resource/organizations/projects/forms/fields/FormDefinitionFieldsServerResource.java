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

package se.streamsource.streamflow.web.resource.organizations.projects.forms.fields;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{index}/fields
 */
public class FormDefinitionFieldsServerResource
      extends CommandQueryServerResource
{
   public ListValue fields()
   {
      String identity = getRequest().getAttributes().get( "forms" ).toString();
      String index = getRequest().getAttributes().get( "index" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      FormsQueries forms = uow.get( FormsQueries.class, identity );

      checkPermission( forms );

      List<ListItemValue> itemValues = forms.applicableFormDefinitionList().items().get();

      ListItemValue value = itemValues.get( Integer.parseInt( index ) );

      FormEntity form = uow.get( FormEntity.class, value.entity().get().identity() );

      return new ListValueBuilder( vbf ).addDescribableItems( form.fields() ).newList();
   }

   public void addField( CreateFieldDTO createFieldDTO )
   {
      String identity = getRequest().getAttributes().get( "forms" ).toString();

      String index = getRequest().getAttributes().get( "index" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      FormsQueries forms = uow.get( FormsQueries.class, identity );

      checkPermission( forms );

      List<ListItemValue> itemValues = forms.applicableFormDefinitionList().items().get();

      ListItemValue value = itemValues.get( Integer.parseInt( index ) );

      FormEntity form = uow.get( FormEntity.class, value.entity().get().identity() );

      form.createField( createFieldDTO.name().get(), getFieldValue( createFieldDTO.fieldType().get() ) );
   }

   private FieldValue getFieldValue( FieldTypes fieldType )
   {
      FieldValue value = null;
      switch (fieldType)
      {
         case text:
            ValueBuilder<FieldValue> valueBuilder = vbf.newValueBuilder( FieldValue.class );
            value = valueBuilder.newInstance();
            break;
         case number:
         case date:
         case single_selection:
         case multi_selection:
         case comment:
         case page_break:

      }
      return value;
   }
}