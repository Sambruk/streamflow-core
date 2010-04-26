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
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Interactions;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.DeleteInteraction;

/**
 * JAVADOC
 */
@Mixins(LabeledContext.Mixin.class)
public interface LabeledContext
   extends DeleteInteraction, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements LabeledContext
   {
      public void delete() throws ResourceException
      {
         context.get( Labelable.class ).removeLabel( context.get( Label.class ));
      }
   }
}
