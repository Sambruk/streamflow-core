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

package se.streamsource.streamflow.web.context.structure.labels;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
@Mixins(LabelContext.Mixin.class)
public interface LabelContext
      extends DescribableContext,
      DeleteContext,
      Context
{
   LinksValue usages();

   abstract class Mixin
         extends ContextMixin
         implements LabelContext
   {
      public LinksValue usages()
      {
         Query<SelectedLabels> usageQuery = roleMap.get( Labels.class).usages( roleMap.get(Label.class) );
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()); // TODO What to use for path here?
         for (SelectedLabels selectedLabels : usageQuery)
         {
            builder.addDescribable( (Describable) selectedLabels );
         }

         return builder.newLinks();
      }

      public void delete()
      {
         Labels labels = roleMap.get( Labels.class );
         Label label = roleMap.get( Label.class );

         labels.removeLabel( label );
      }
   }
}
