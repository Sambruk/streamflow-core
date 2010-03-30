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

package se.streamsource.streamflow.domain;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.GroupedListItemValue;

/**
 * Builder for making it easier to create ListValue/ListItemValue
 */
public class ListValueBuilder
{
   private ValueBuilder<ListValue> listBuilder;
   private ValueBuilder<ListItemValue> itemBuilder;
   private ValueBuilder<GroupedListItemValue> groupedItemBuilder;
   private ValueBuilderFactory vbf;

   public ListValueBuilder( ValueBuilderFactory vbf )
   {
      this.vbf = vbf;
      listBuilder = vbf.newValueBuilder( ListValue.class );
      itemBuilder = vbf.newValueBuilder( ListItemValue.class );
   }

   public ListValueBuilder addListItem( String description, EntityReference ref )
   {
      itemBuilder.prototype().description().set( description );
      itemBuilder.prototype().entity().set( ref );

      listBuilder.prototype().items().get().add( itemBuilder.newInstance() );

      return this;
   }

   public ListValueBuilder addListItem( String description, EntityReference ref, String groupDescription )
   {
      if (groupedItemBuilder == null)
         groupedItemBuilder = vbf.newValueBuilder( GroupedListItemValue.class );

      groupedItemBuilder.prototype().description().set( description );
      groupedItemBuilder.prototype().entity().set( ref );
      groupedItemBuilder.prototype().group().set( groupDescription );

      listBuilder.prototype().items().get().add( groupedItemBuilder.newInstance() );

      return this;
   }

   public ListValueBuilder addDescribableItems( Iterable<? extends Describable> items )
   {
      for (Describable item : items)
      {
         addListItem( item.getDescription(), EntityReference.getEntityReference( item ) );
      }

      return this;
   }

   public ListValueBuilder addDescribable( Describable item )
   {
      return addListItem( item.getDescription(), EntityReference.getEntityReference( item ) );
   }

   public ListValueBuilder addDescribable( Describable item, Describable group )
   {
      return addDescribable( item, group.getDescription() );
   }

   public ListValueBuilder addDescribable( Describable item, String group )
   {
      return addListItem( item.getDescription(), EntityReference.getEntityReference( item ), group );
   }

   public ListValue newList()
   {
      return listBuilder.newInstance();
   }
}
