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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.structure.form.FormId;

/**
 * Information about a single form
 */
@Mixins(FormInfoContext.Mixin.class)
public interface FormInfoContext
      extends DescribableContext, NotableContext, IndexContext<FormValue>, Context
{
   void changeformid( StringValue stringValue );

   abstract class Mixin
         extends ContextMixin
         implements FormInfoContext
   {
      public FormValue index()
      {
         FormEntity form = roleMap.get( FormEntity.class );

         ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );

         builder.prototype().note().set( form.note().get() );
         builder.prototype().description().set( form.description().get() );
         builder.prototype().form().set( EntityReference.parseEntityReference( form.identity().get() ) );
         builder.prototype().id().set( form.formId().get() );

         return builder.newInstance();
      }

      public void changeformid( StringValue stringValue )
      {
         FormId form = roleMap.get( FormId.class );
         form.changeFormId( stringValue.string().get() );
      }
   }
}
