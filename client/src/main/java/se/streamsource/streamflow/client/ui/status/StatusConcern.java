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

package se.streamsource.streamflow.client.ui.status;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.Service;

import java.lang.reflect.Method;

/**
 * Set the statusbar message. Triggered by using the {@link StatusMessage} annotation
 * on a method.
 */
@AppliesTo(StatusMessage.class)
public class StatusConcern
      extends GenericConcern
{
   @Invocation
   StatusMessage message;

   @Service
   StatusService status;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      status.status( message.value() );

      return next.invoke( proxy, method, args );
   }
}
