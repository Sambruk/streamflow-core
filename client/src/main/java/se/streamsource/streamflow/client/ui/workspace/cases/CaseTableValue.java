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
package se.streamsource.streamflow.client.ui.workspace.cases;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;

import java.util.Date;

/**
 * JAVADOC
 */
public interface CaseTableValue
      extends ValueComposite
{
   Property<String> description();

   Property<Date> creationDate();

   @Optional
   Property<String> createdBy();

   @Optional
   Property<Date> dueOn();

   @Optional
   Property<String> caseId();

   Property<CaseStates> status();

   @Optional
   Property<String> owner();

   Property<LinksValue> labels();

   @Optional
   Property<String> caseType();

   @Optional
   Property<String> assignedTo();

   @Optional
   Property<String> resolution();

   @UseDefaults
   Property<Boolean> hasContacts();

   @UseDefaults
   Property<Boolean> hasConversations();

   @UseDefaults
   Property<Boolean> hasUnreadConversation();

   @UseDefaults
   Property<Boolean> hasSubmittedForms();

   @UseDefaults
   Property<Boolean> hasUnreadForm();

   @UseDefaults
   Property<Boolean> hasAttachments();

   Property<LinksValue> subcases();

   @Optional
   Property<LinkValue> parentCase();

   Property<String> href();

   @UseDefaults
   Property<Boolean> removed();

   @Optional
   Property<PriorityValue> priority();

   @UseDefaults
   Property<Boolean> unread();
}