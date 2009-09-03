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

import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * Query that restricts what events to return
 */
public class EventQuery
        implements EventSpecification
{
    private Date afterDate; // Only return events after this date
    private String name; // Only return events with this name
    private String entity; // Only return events on this entity
    private String by; // Only return events caused by this user

    public EventQuery(Date afterDate, String name, String entity, String by)
    {
        this.afterDate = afterDate;
        this.name = name;
        this.entity = entity;
        this.by = by;
    }

    public Date afterDate()
    {
        return afterDate;
    }

    public String name()
    {
        return name;
    }

    public String entity()
    {
        return entity;
    }

    public String by()
    {
        return by;
    }

    public boolean accept(DomainEvent event)
    {
        // Check criteria
        if (afterDate != null && event.on().get().before(afterDate))
            return false;

        if (name != null && !event.name().get().equals(name))
            return false;

        if (entity != null && !event.entity().get().equals(entity))
            return false;

        if (by != null && !event.by().get().equals(by))
            return false;

        return true; // Event is accepted
    }
}
