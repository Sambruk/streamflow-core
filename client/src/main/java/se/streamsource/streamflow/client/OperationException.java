/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client;

import se.streamsource.streamflow.client.util.i18n;

/**
 * Exception thrown by model in the client if something goes wrong in accessing the server-side REST API.
 * <p/>
 * The constructor requires that you provide an enumeration key as the message. This is then used with the
 * {@link i18n} class to get the actual message. This ensures that all messages use i18n properly.
 */
public class OperationException
      extends RuntimeException
{
   public OperationException( Enum messageEnum, Throwable cause )
   {
      super( i18n.text( messageEnum ), cause );
   }
}
