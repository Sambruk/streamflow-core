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

package se.streamsource.streamflow.web.domain.structure.project.filter;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.List;

/**
 * TODO
 */
@Mixins(Filters.Mixin.class)
public interface Filters
{
   void addFilter(FilterValue filter);

   void updateFilter(int idx, FilterValue filter);

   void removeFilter(FilterValue filter);

   interface Data
   {
      @UseDefaults
      Property<List<FilterValue>> filters();
   }

   interface Events
   {
      public void addedFilter(@Optional DomainEvent event, FilterValue filter);
      public void updatedFilter(@Optional DomainEvent event, int idx, FilterValue filter);
      public void removedFilter(@Optional DomainEvent event, FilterValue filter);
   }

   class Mixin
      implements Filters, Events
   {
      @This
      Data data;

      public void addFilter(FilterValue filter)
      {
         addedFilter(null, filter);
      }

      public void updateFilter(int idx, FilterValue filter)
      {
         if (idx >= 0 && idx < data.filters().get().size())
         {
            updatedFilter(null, idx, filter);
         }
      }

      public void removeFilter(FilterValue filter)
      {
         if (data.filters().get().contains(filter))
            removedFilter(null, filter);
      }

      public void addedFilter(@Optional DomainEvent event, FilterValue filter)
      {
         List<FilterValue> filterValues = data.filters().get();
         filterValues.add(filter);
         data.filters().set(filterValues);
      }

      public void updatedFilter(@Optional DomainEvent event, int idx, FilterValue filter)
      {
         List<FilterValue> filterValues = data.filters().get();
         filterValues.set(idx, filter);
         data.filters().set(filterValues);
      }

      public void removedFilter(@Optional DomainEvent event, FilterValue filter)
      {
         List<FilterValue> filterValues = data.filters().get();
         filterValues.remove(filter);
         data.filters().set(filterValues);
      }
   }
}
