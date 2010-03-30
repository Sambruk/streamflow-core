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
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.label.PossibleLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(SelectedLabelsContext.Mixin.class)
public interface SelectedLabelsContext
   extends SubContexts<SelectedLabelContext>, IndexContext<LinksValue>, Context
{
   public LinksValue possiblelabels();
   public void createlabel( StringValue name );
   public void addlabel( EntityReferenceDTO labelDTO );

   abstract class Mixin
         extends ContextMixin
         implements SelectedLabelsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         SelectedLabels.Data labels = context.role(SelectedLabels.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "label" ).addDescribables( labels.selectedLabels() ).newLinks();
      }

      public LinksValue possiblelabels()
      {
         PossibleLabelsQueries possibleLabelsQueries = context.role(PossibleLabelsQueries.class);
         Labels.Data labels = context.role( Labels.Data.class);

         return new LinksBuilder(module.valueBuilderFactory()).command( "addlabel" ).addDescribables( possibleLabelsQueries.possibleLabels( labels.labels() )).newLinks();
      }

      public void createlabel( StringValue name )
      {
         Labels labels = context.role(Labels.class);
         SelectedLabels selectedLabels = context.role(SelectedLabels.class);

         Label label = labels.createLabel( name.string().get() );
         selectedLabels.addSelectedLabel( label );
      }

      public void addlabel( EntityReferenceDTO labelDTO )
      {
         SelectedLabels labels = context.role( SelectedLabels.class);
         Label label = module.unitOfWorkFactory().currentUnitOfWork().get( Label.class, labelDTO.entity().get().identity() );

         labels.addSelectedLabel( label );
      }

      public SelectedLabelContext context( String id )
      {
         context.playRoles( module.unitOfWorkFactory().currentUnitOfWork().get(Label.class, id ));
         return subContext( SelectedLabelContext.class );
      }
   }
}
