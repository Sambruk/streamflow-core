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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.Event;

/**
 * JAVADOC
 */
@Mixins(IsRead.IsReadMixin.class)
public interface IsRead
{
    void markAsRead();

    void markAsUnread();

    interface IsReadState
    {
        @UseDefaults
        Property<Boolean> isRead();

        @Event
        void markedAsUnread(DomainEvent event);

        @Event
        void markedAsRead(DomainEvent event);
    }

    public abstract class IsReadMixin
            implements IsRead, IsReadState
    {
        public void markAsRead()
        {
            if (!isRead().get())
                markedAsRead(DomainEvent.CREATE);
        }

        public void markAsUnread()
        {
            if (isRead().get())
                markedAsUnread(DomainEvent.CREATE);
        }

        public void markedAsUnread(DomainEvent event)
        {
            isRead().set(false);
        }

        public void markedAsRead(DomainEvent event)
        {
            isRead().set(true);
        }
    }
}
