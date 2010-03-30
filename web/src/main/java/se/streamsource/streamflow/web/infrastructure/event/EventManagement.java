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

package se.streamsource.streamflow.web.infrastructure.event;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;

/**
 * Interface for performing management operations on the EventStore.
 */
public interface EventManagement
{
   /**
    * Remove all events from the EventStore.
    *
    * @throws IOException
    */
   void removeAll() throws IOException;

   /**
    * Remove all events up to and including the given date. This is used
    * to clean out old events that are no longer needed.
    *
    * @param date
    * @throws IOException
    */
   void removeTo( Date date ) throws IOException;

   /**
    * Does restore events from the given reader. 
    *
    * @param in
    * @throws IOException
    */
   void restoreEvents( Reader in ) throws IOException;
}
