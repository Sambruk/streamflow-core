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
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.CloseActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationParticipantsQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * TODO
 */
public class ActionsContext
   implements IndexContext<Iterable<ActionValue>>
{
   @Structure
   Module module;

   @Uses
   Filters filterable;

   @Uses
   FilterValue filter;

   @Uses Integer index;

   public Iterable<ActionValue> index()
   {
      return filter.actions().get();
   }

   public Iterable<Member> possibleRecipients()
   {
      Members.Data members = RoleMap.role(Members.Data.class);
      return members.members();
   }

   public void createEmail(Member member)
   {
      ValueBuilder<EmailActionValue> builder = module.valueBuilderFactory().newValueBuilder(EmailActionValue.class);
      builder.prototype().participant().set(EntityReference.getEntityReference(member));

      ValueBuilder<FilterValue> filterBuilder = filter.buildWith();
      filterBuilder.prototype().actions().get().add(builder.newInstance());

      filterable.updateFilter(index, filterBuilder.newInstance());
   }
   
   public void closeCase()
   {
      ValueBuilder<CloseActionValue> builder = module.valueBuilderFactory().newValueBuilder(CloseActionValue.class);

      ValueBuilder<FilterValue> filterBuilder = filter.buildWith();
      filterBuilder.prototype().actions().get().add(builder.newInstance());

      filterable.updateFilter(index, filterBuilder.newInstance());
   }
}
