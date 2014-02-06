/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.policy;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.util.LinkValueListModel;

/**
 * JAVADOC
 */
public class AdministratorsModel
      extends LinkValueListModel
{

   public void addAdministrator( LinkValue link )
   {
      client.postLink( link );
   }

   public UsersAndGroupsModel newUsersAndGroupsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder( UsersAndGroupsModel.class ).use( client ).newInstance();
   }
}