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

package se.streamsource.streamflow.web.context.workspace.savedsearch;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.interaction.profile.SavedSearches;
import se.streamsource.streamflow.web.domain.structure.user.profile.SavedSearch;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class SavedSearchContext
      implements DeleteContext
{
   public void delete() throws ResourceException
   {
      role( SavedSearches.class ).removeSavedSearch( RoleMap.role( SavedSearch.class ) );
   }

   public void changeQuery( StringValue query )
   {
      SavedSearch savedSearch = role( SavedSearch.class );
      savedSearch.changeQuery( query.string().get() );
   }
}
