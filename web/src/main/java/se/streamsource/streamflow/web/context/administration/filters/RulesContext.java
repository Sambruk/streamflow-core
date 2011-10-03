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

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.administration.filter.AssignActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static se.streamsource.dci.api.RoleMap.role;

/**
 * TODO
 */
public class RulesContext
   implements IndexContext<Iterable<RuleValue>>
{
   @Structure
   Module module;

   @Uses
   Filters filterable;

   @Uses
   FilterValue filter;

   @Uses Integer index;

   public Iterable<RuleValue> index()
   {
      return filter.rules().get();
   }

   public Map<Label, Describable> possibleLabels()
   {
      OrganizationQueries orgQueries = role(OrganizationQueries.class);
      final SelectedLabels.Data selectedLabels = role(SelectedLabels.Data.class);
      final Map<Label, Describable> labels = new LinkedHashMap<Label, Describable>();
      orgQueries.visitOrganization(new OrganizationVisitor()
      {
         Describable owner;

         @Override
         public boolean visitOrganization(Organization org)
         {
            owner = org;
            return super.visitOrganization(org);
         }

         @Override
         public boolean visitOrganizationalUnit(OrganizationalUnit ou)
         {
            owner = ou;

            return super.visitOrganizationalUnit(ou);
         }

         @Override
         public boolean visitProject(Project project)
         {
            owner = project;

            return super.visitProject(project);
         }

         @Override
         public boolean visitCaseType(CaseType caseType)
         {
            owner = caseType;

            return super.visitCaseType(caseType);
         }

         @Override
         public boolean visitLabel(Label label)
         {
            if (!selectedLabels.selectedLabels().contains(label))
               labels.put(label, owner);

            return true;
         }
      }, new OrganizationQueries.ClassSpecification(Organization.class,
            OrganizationalUnits.class,
            OrganizationalUnit.class,
            Projects.class,
            Project.class,
            CaseTypes.class,
            CaseType.class,
            Labels.class));

      return labels;
   }

   public void createLabel(@Name("entity") Label label)
   {
      ValueBuilder<LabelRuleValue> builder = module.valueBuilderFactory().newValueBuilder(LabelRuleValue.class);
      builder.prototype().label().set(EntityReference.getEntityReference(label));

      ValueBuilder<FilterValue> filterBuilder = filter.buildWith();
      filterBuilder.prototype().rules().get().add(builder.newInstance());

      filterable.updateFilter(index, filterBuilder.newInstance());
   }
}
