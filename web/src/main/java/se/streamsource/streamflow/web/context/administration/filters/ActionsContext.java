/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration.filters;

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
import se.streamsource.streamflow.api.administration.filter.EmailNotificationActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

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

   public Iterable<Member> possibleNotificationRecipients()
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

   public void createEmailNotification(Member member)
   {
      ValueBuilder<EmailNotificationActionValue> builder = module.valueBuilderFactory().newValueBuilder(EmailNotificationActionValue.class);
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
