/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.web.domain.entity.form.*;
import se.streamsource.streamflow.web.domain.structure.form.*;

/**
 * Information about a single form
 */
public class FormInfoContext
      implements IndexContext<FormValue>
{
   @Structure
   Module module;

   public FormValue index()
   {
      FormEntity form = RoleMap.role( FormEntity.class );

      ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );

      builder.prototype().note().set( form.note().get() );
      builder.prototype().description().set( form.description().get() );
      builder.prototype().form().set( EntityReference.parseEntityReference( form.identity().get() ) );
      builder.prototype().id().set( form.formId().get() );

      return builder.newInstance();
   }

   public void changeformid( StringValue stringValue )
   {
      FormId form = RoleMap.role( FormId.class );
      form.changeFormId( stringValue.string().get() );
   }
}
