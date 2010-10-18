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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.resource.user.profile.SearchValue;
import se.streamsource.streamflow.web.domain.interaction.profile.SavedSearches;
import se.streamsource.streamflow.web.domain.structure.user.profile.SavedSearch;

/**
 * JAVADOC
 */
@Mixins(SavedSearchesContext.Mixin.class)
public interface SavedSearchesContext
      extends SubContexts<SavedSearchContext>, IndexContext<LinksValue>, Context
{
   public void createsearch( SearchValue search );

   abstract class Mixin
         extends ContextMixin
         implements IndexContext<LinksValue>,
         SavedSearchesContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         TitledLinksBuilder builder = new TitledLinksBuilder( module.valueBuilderFactory() );
         SavedSearches.Data searches = roleMap.get( SavedSearches.Data.class );
         for (SavedSearch search : searches.searches().toList())
         {
            builder.addDescribable( search, ((SavedSearch.Data) search).query().get() );
         }

         return builder.newLinks();
      }

      public void createsearch( SearchValue search )
      {
         SavedSearches searches = roleMap.get( SavedSearches.class );
         searches.createSavedSearch( search );
      }

      public SavedSearchContext context( String id ) throws ContextNotFoundException
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( SavedSearch.class, id ) );
         return subContext( SavedSearchContext.class );
      }
   }
}
