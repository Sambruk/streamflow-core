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

package se.streamsource.streamflow.client;

import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.infrastructure.event.RemoteEventNotification;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;

import java.rmi.RemoteException;

/**
 * JAVADOC
 */
public class RemoteEventNotificationImpl
    implements RemoteEventNotification
{
    @Service
    EventSourceListener esl;

    public void notifyEvents() throws RemoteException
    {
        System.out.println("NOTIFY EVENTS");
    }
}
