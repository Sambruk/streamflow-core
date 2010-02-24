/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.structure.labels;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContexts;

import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(LabelableContext.Mixin.class)
public interface LabelableContext
   extends SubContexts<LabeledContext>, Context
{
   LinksValue possiblelabels();
   void addlabel( EntityReferenceDTO reference );

   abstract class Mixin
      extends ContextMixin
      implements LabelableContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public LinksValue possiblelabels()
      {
         TaskLabelsQueries labels = context.role(TaskLabelsQueries.class);

         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command( "label" );
         for (Map.Entry<Label, SelectedLabels> labelSelectedLabelsEntry : labels.possibleLabels().entrySet())
         {
            builder.addDescribable( labelSelectedLabelsEntry.getKey(), (Describable) labelSelectedLabelsEntry.getValue() );
         }
         return builder.newLinks();
      }

      public void addlabel( EntityReferenceDTO reference )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         Labelable labelable = context.role( Labelable.class );
         Label label = uow.get( Label.class, reference.entity().get().identity() );

         labelable.addLabel( label );
      }

      public LabeledContext context( String id )
      {
         context.playRoles(module.unitOfWorkFactory().currentUnitOfWork().get( Label.class, id ));
         return subContext( LabeledContext.class );
      }
   }
}