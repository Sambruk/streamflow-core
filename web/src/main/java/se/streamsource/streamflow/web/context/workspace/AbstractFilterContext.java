/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.HashSet;


/**
 * General interface for fetching filter data for contexts related through project i.e. Inbox and Assignment
 */
@Mixins(AbstractFilterContext.Mixin.class)
public interface AbstractFilterContext extends Context
{
   public LinksValue possibleCaseTypes();

   public LinksValue possibleLabels();

   abstract class Mixin
           implements AbstractFilterContext
   {
      @Structure
      Module module;

      public LinksValue possibleLabels()
      {
         // Fetch all labels from CaseType's ---> Organization
         HashSet<Object> labels = new HashSet<Object>();

         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
         Project project = RoleMap.role(Project.class);

         // labels from project's selected case types
         for (CaseType caseType : ((SelectedCaseTypes.Data) project).selectedCaseTypes())
         {
            labels.addAll(((SelectedLabels.Data) caseType).selectedLabels().toSet());
         }

         // project's selected labels
         labels.addAll(((SelectedLabels.Data) project).selectedLabels().toSet());


         // OU hirarchy labels from bottom up
         Entity entity = (Entity) ((Ownable.Data) project).owner().get();

         while (entity instanceof Ownable)
         {
            labels.addAll(((SelectedLabels.Data) entity).selectedLabels().toSet());
            entity = (Entity) ((Ownable.Data) entity).owner().get();
         }
         // Organization's selected labels
         labels.addAll(((SelectedLabels.Data) entity).selectedLabels().toSet());

         for (Object label : labels)
         {
            builder.addDescribable((Label) label);
         }
         return builder.newLinks();
      }

      public LinksValue possibleCaseTypes()
      {
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
         Project project = RoleMap.role(Project.class);
         SelectedCaseTypes.Data selectedCaseTypes = (SelectedCaseTypes.Data) project;

         for (CaseType caseType : selectedCaseTypes.selectedCaseTypes())
         {
            builder.addDescribable(caseType);
         }
         return builder.newLinks();
      }
   }
}
