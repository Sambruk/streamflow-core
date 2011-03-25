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

package se.streamsource.streamflow.client.domain.individual;

import org.restlet.Uniform;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;

/**
 * JAVADOC
 */
public interface AccountSettings
{
   AccountSettingsValue accountSettings();

   void updateSettings( AccountSettingsValue newAccountSettings );

   void changePassword( Uniform client, ChangePasswordCommand changePassword ) throws ResourceException;
}
