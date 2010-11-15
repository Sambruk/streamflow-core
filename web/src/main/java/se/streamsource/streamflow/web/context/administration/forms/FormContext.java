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

package se.streamsource.streamflow.web.context.administration.forms;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

/**
 * JAVADOC
 */
public class FormContext
      implements DeleteContext, IndexContext<FormValue>
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

   public void move( EntityValue to )
   {
      Forms toForms = module.unitOfWorkFactory().currentUnitOfWork().get( Forms.class, to.entity().get() );
      Form form = RoleMap.role( Form.class );
      RoleMap.role( Forms.class ).moveForm( form, toForms );
   }

   public LinksValue usages()
   {
      Query<SelectedForms> usageQuery = RoleMap.role( Forms.class ).usages( RoleMap.role( Form.class ) );
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      return builder.addDescribables( (Iterable<? extends Describable>) usageQuery ).newLinks();
   }

   public void delete()
   {
      Form form = RoleMap.role( Form.class );
      Forms forms = RoleMap.role( Forms.class );
      forms.removeForm( form );
   }
}
