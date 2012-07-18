/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.api.administration.form.FormValue;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

import static se.streamsource.dci.api.RoleMap.role;

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

   public Iterable<Forms> possiblemoveto()
   {
      final Forms thisForms = role(Forms.class);

      return Iterables.filter( new Specification<Forms>()
      {
         public boolean satisfiedBy( Forms item )
         {
            return !item.equals(thisForms);
         }
      }, module.queryBuilderFactory().newQueryBuilder( Forms.class ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() ));
   }

   public void move( EntityValue to )
   {
      Forms toForms = module.unitOfWorkFactory().currentUnitOfWork().get( Forms.class, to.entity().get() );
      Form form = RoleMap.role( Form.class );
      RoleMap.role( Forms.class ).moveForm( form, toForms );
   }

   public Iterable<SelectedForms> usages()
   {
      return RoleMap.role( Forms.class ).usages( RoleMap.role( Form.class ) );
/*
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      return builder.addDescribables( (Iterable<Describable>) usageQuery ).newLinks();
*/
   }

   public void delete()
   {
      Form form = RoleMap.role( Form.class );
      Forms forms = RoleMap.role( Forms.class );
      forms.removeForm( form );
   }
}
