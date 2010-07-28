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

package se.streamsource.streamflow.web.context.users.workspace;

import org.qi4j.api.mixin.Mixins;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.interaction.profile.SavedSearches;
import se.streamsource.streamflow.web.domain.structure.user.profile.SavedSearch;

/**
 * JAVADOC
 */
@Mixins(SavedSearchContext.Mixin.class)
public interface SavedSearchContext
      extends DescribableContext,
      DeleteInteraction,
      Interactions
{
   public void changedescription( StringValue name );

   public void changequery( StringValue query );

   abstract class Mixin
         extends InteractionsMixin
         implements SavedSearchContext
   {
      public void delete() throws ResourceException
      {
         context.get( SavedSearches.class ).removeSavedSearch( context.get( SavedSearch.class ) );
      }

      public void changequery( StringValue query )
      {
         SavedSearch savedSearch = context.get( SavedSearch.class );
         savedSearch.changeQuery( query.string().get() );
      }

      public void changedescription( StringValue name )
      {
         SavedSearch savedSearch = context.get( SavedSearch.class );
         savedSearch.changeDescription( name.string().get() );
      }
   }
}
