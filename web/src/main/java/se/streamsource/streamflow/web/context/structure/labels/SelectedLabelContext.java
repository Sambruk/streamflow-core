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
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.dci.api.DeleteInteraction;

/**
 * JAVADOC
 */
@Mixins(SelectedLabelContext.Mixin.class)
public interface SelectedLabelContext
   extends DeleteInteraction, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements SelectedLabelContext
   {
      public void delete()
      {
         SelectedLabels labels = context.get( SelectedLabels.class);
         Label label = context.get( Label.class);

         labels.removeSelectedLabel( label );
      }

   }
}