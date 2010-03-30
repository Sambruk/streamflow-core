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

package se.streamsource.streamflow.web.context.structure.labels;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(LabelsContext.Mixin.class)
public interface LabelsContext
   extends Context, SubContexts<LabelContext>
{
   LinksValue labels();
   void createlabel( StringValue name );

   abstract class Mixin
      extends ContextMixin
      implements LabelsContext
   {
      @Structure
      Module module;

      public LinksValue labels()
      {
         return new LinksBuilder(module.valueBuilderFactory()).rel( "label" ).addDescribables( context.role(Labels.class).getLabels()).newLinks();
      }

      public void createlabel( StringValue name )
      {
         Labels labels = context.role(Labels.class);

         labels.createLabel( name.string().get() );
      }

      public LabelContext context( String id )
      {
         context.playRoles(module.unitOfWorkFactory().currentUnitOfWork().get( Label.class, id ));

         return subContext( LabelContext.class );
      }
   }
}
