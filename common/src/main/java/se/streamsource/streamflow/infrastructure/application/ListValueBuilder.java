/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.infrastructure.application;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.roles.Describable;

/**
 * Builder for making it easier to create ListValue/ListItemValue
 */
public class ListValueBuilder
{
   private ValueBuilder<ListValue> listBuilder;
   private ValueBuilder<ListItemValue> itemBuilder;

   public ListValueBuilder( ValueBuilderFactory vbf )
   {
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

   public ListValue newList()
   {
      return listBuilder.newInstance();
   }
}
