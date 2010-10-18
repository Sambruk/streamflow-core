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
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;

/**
 * JAVADOC
 */
@Mixins(FormContext.Mixin.class)
public interface FormContext
   extends DeleteContext, Context, IndexContext<FormValue>
{
   void move( EntityValue to);

   @SubContext
   FormInfoContext forminfo();

   @SubContext
   FormPagesContext pages();

   @SubContext
   FormSignaturesContext signatures();

   abstract class Mixin
      extends ContextMixin
      implements FormContext
   {
      public FormValue index()
      {
         FormEntity form = roleMap.get(FormEntity.class);

         ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );

         builder.prototype().note().set( form.note().get() );
         builder.prototype().description().set( form.description().get() );
         builder.prototype().form().set( EntityReference.parseEntityReference( form.identity().get() ) );
         builder.prototype().id().set( form.formId().get() );

         return builder.newInstance();
      }

      public void move(EntityValue to)
      {
         Forms toForms = module.unitOfWorkFactory().currentUnitOfWork().get( Forms.class, to.entity().get() );
         Form form = roleMap.get(Form.class);
         roleMap.get( Forms.class ).moveForm(form, toForms);
      }

      public void delete()
      {
         Form form = roleMap.get( Form.class);
         Forms forms = roleMap.get(Forms.class);
         forms.removeForm( form );
      }

      public FormInfoContext forminfo()
      {
         return subContext( FormInfoContext.class );
      }

      public FormPagesContext pages()
      {
         return subContext( FormPagesContext.class );
      }

      public FormSignaturesContext signatures()
      {
         return subContext( FormSignaturesContext.class );
      }
   }
}
