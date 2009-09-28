/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.infrastructure.event.source;

import org.qi4j.api.common.Optional;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * JAVADOC
 */
public interface EventStore
{
    /**
     * Get list of events that matches the parameters. Note that events will only be included if they have occurred
     * after the given date. If they are on the exact same date, they will not be included. Also, maxEvents specifies
     * after how many events there will be a cutoff, but the rule is also that UnitsOfWork are provided in their
     * entirety. As a consequence of this the returned nr of events might be slightly higher than maxEvents, in order
     * to satisfy this rule.
     * 
     * @param specification
     * @param afterDate
     * @param maxEvents
     * @return
     */
    Iterable<DomainEvent> events(@Optional EventSpecification specification, @Optional Date afterDate, int maxEvents);
}
