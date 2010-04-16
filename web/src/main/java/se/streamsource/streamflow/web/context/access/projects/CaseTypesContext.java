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

package se.streamsource.streamflow.web.context.access.projects;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;

/**
 * JAVADOC
 */
@Mixins(CaseTypesContext.Mixin.class)
public interface CaseTypesContext
   extends SubContexts<LabelsContext>, IndexContext<LinksValue>, Context
{
   abstract class Mixin
      extends ContextMixin
      implements CaseTypesContext
   {
      public LinksValue index()
      {
         SelectedTaskTypes.Data data = context.role( SelectedTaskTypes.Data.class );
         Describable describable = context.role( Describable.class );

         TitledLinksBuilder builder = new TitledLinksBuilder( module.valueBuilderFactory() );

         builder.addDescribables( data.selectedTaskTypes() );
         builder.addTitle( describable.getDescription() );

         return builder.newLinks();
      }

      public LabelsContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get( TaskType.class, id ) );

         return subContext( LabelsContext.class);
      }
   }
}