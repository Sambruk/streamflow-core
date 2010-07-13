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

package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;

import java.util.Date;
import java.util.List;

/**
 * Statistics for a single closed case
 */
public interface CaseStatisticsValue
{
   Property<String> identity();
   Property<String> caseId();
   Property<String> description();
   Property<String> note();
   Property<Date> createdOn();
   Property<Date> closedOn();
   Property<Long> duration();

   Property<List<String>> labels();

   Property<String> assigneeId();
   @Optional Property<String> caseTypeId();
   @Optional Property<String> caseTypeOwnerId();
   @Optional Property<String> organizationalUnit();
   @Optional Property<String> groupId();
   @Optional Property<String> resolutionId();
}
