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

package se.streamsource.streamflow.resource.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;

import java.util.Date;

/**
 * JAVADOC
 */
public interface CaseValue
      extends LinkValue
{
   Property<Date> creationDate();

   @Optional
   Property<String> createdBy();

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
}