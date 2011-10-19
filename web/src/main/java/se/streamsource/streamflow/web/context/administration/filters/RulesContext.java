/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.administration.filters;

import java.util.HashSet;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * TODO
 */
public class RulesContext implements IndexContext<Iterable<RuleValue>>
{
   @Structure
   Module module;

   @Uses
   Project project;

   @Uses
   FilterValue filter;

   @Uses
   Integer index;

   public Iterable<RuleValue> index()
   {
      return filter.rules().get();
   }

   public LinksValue possibleLabels()
   {
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "createlabel" );
      HashSet<Object> labels = new HashSet<Object>();

      // project's selected labels
      labels.addAll( ((SelectedLabels.Data) project).selectedLabels().toSet() );

      // OU hirarchy labels from bottom up
      Entity entity = (Entity) ((Ownable.Data) project).owner().get();

      while (entity instanceof Ownable)
      {
         labels.addAll( ((SelectedLabels.Data) entity).selectedLabels().toSet() );
         entity = (Entity) ((Ownable.Data) entity).owner().get();
      }
      // Organization's selected labels
      labels.addAll( ((SelectedLabels.Data) entity).selectedLabels().toSet() );

      // SelectedLabels on selectedCaseTypes
      for (CaseType caseType : ((SelectedCaseTypes.Data) project).selectedCaseTypes())
      {
         labels.addAll( ((SelectedLabels.Data) caseType).selectedLabels().toSet() );
      }

      for (Object object : labels)
      {
         Label label = (Label) object;

         boolean foundLabel = false;
         for (RuleValue rule : filter.rules().get())
         {
            if (rule instanceof LabelRuleValue)
            {
               if (((LabelRuleValue) rule).label().get().identity()
                     .equals( EntityReference.getEntityReference( label ).identity() ))
               {
                  foundLabel = true;
                  break;
               }
            }
         }
         if (!foundLabel)
         {
            builder.addDescribable( label );
         }
      }
      return builder.newLinks();
   }

   public void createLabel(@Name("entity") Label label)
   {
      ValueBuilder<LabelRuleValue> builder = module.valueBuilderFactory().newValueBuilder( LabelRuleValue.class );
      builder.prototype().label().set( EntityReference.getEntityReference( label ) );

      ValueBuilder<FilterValue> filterBuilder = filter.buildWith();
      filterBuilder.prototype().rules().get().add( builder.newInstance() );

      project.updateFilter( index, filterBuilder.newInstance() );
   }
}
