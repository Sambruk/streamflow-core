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

import java.util.Date;

/**
 * An EventStore is a store of events. Events are grouped in the transactions in which they were created.
 */
public interface EventStore
{
    /**
     * Get list of event transactions after the given timestamp. If they are on the exact same timestamp, they will not be included.
     *
     * The method uses double-dispatch, so a handler is sent in which is given each transaction, one at a time.
     *
     * @param afterTimestamp
     * @param handler
     * @return
     */
    void transactions(@Optional Date afterTimestamp, TransactionHandler handler);
}
