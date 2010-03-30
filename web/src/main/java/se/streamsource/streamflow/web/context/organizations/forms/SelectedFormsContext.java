/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.context.organizations.SelectedTaskTypeContext;
import se.streamsource.streamflow.web.domain.entity.form.PossibleFormsQueries;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;

/**
 * JAVADOC
 */
@Mixins(SelectedFormsContext.Mixin.class)
public interface SelectedFormsContext
   extends SubContexts<SelectedFormContext>, IndexContext<LinksValue>, Context
{
   public LinksValue possibleforms();

   public void addform( EntityReferenceDTO taskTypeDTO );

   abstract class Mixin
      extends ContextMixin
      implements SelectedFormsContext
   {
      public LinksValue index()
      {
         SelectedForms.Data forms = context.role(SelectedForms.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel("selectedform").addDescribables( forms.selectedForms() ).newLinks();
      }

      public LinksValue possibleforms()
      {
         PossibleFormsQueries possibleForms = context.role(PossibleFormsQueries.class);

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addform" );
         possibleForms.possibleForms( builder);

         return builder.newLinks();
      }

      public void addform( EntityReferenceDTO formDTO )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         SelectedForms selectedForms = context.role(SelectedForms.class);
         Form form = uow.get( Form.class, formDTO.entity().get().identity() );

         selectedForms.addSelectedForm( form );
      }

      public SelectedFormContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, id ));
         return subContext( SelectedFormContext.class );
      }
   }
}