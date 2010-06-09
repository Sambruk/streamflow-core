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

package se.streamsource.streamflow.infrastructure.application;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.domain.structure.Describable;

/**
 * Builder for making it easier to create LinksValue/LinkValue in a Streamflow context
 */
public class LinksBuilder
   extends se.streamsource.dci.value.LinksBuilder
{
   public LinksBuilder( ValueBuilderFactory vbf )
   {
      super(vbf);
   }

   @Override
   public LinksBuilder path( String subPath )
   {
      super.path( subPath );
      return this;
   }

   @Override
   public LinksBuilder rel( String rel )
   {
      super.rel( rel );
      return this;
   }

   @Override
   public LinksBuilder command( String commandName )
   {
      super.command( commandName );
      return this;
   }

   @Override
   public LinksBuilder addLink( LinkValue linkValue )
   {
      super.addLink( linkValue );
      return this;
   }

   @Override
   public LinksBuilder addLink( String description, EntityReference ref )
   {
      super.addLink( description, ref );
      return this;
   }

   @Override
   public LinksBuilder addLink( String description, String id )
   {
      super.addLink( description, id );
      return this;
   }

   @Override
   public LinksBuilder addLink( String description, String id, String rel, String href, String classes )
   {
      super.addLink( description, id, rel, href, classes );
      return this;
   }

   @Override
   public LinksBuilder addLink( String description, EntityReference ref, String title, String classes )
   {
      super.addLink( description, ref, title, classes );
      return this;
   }

   public LinksBuilder addDescribables( Iterable<? extends Describable> items )
   {
      for (Describable item : items)
      {
         addLink( item.getDescription(), EntityReference.getEntityReference( item ) );
      }

      return this;
   }

   public LinksBuilder addDescribable( Describable item )
   {
      addLink( item.getDescription(), EntityReference.getEntityReference( item ) );
      return this;
   }

   public LinksBuilder addDescribable( Describable item, Describable group )
   {
      return addDescribable( item, group.getDescription() );
   }

   public LinksBuilder addDescribable( Describable item, String group )
   {
      addLink( item.getDescription(), EntityReference.getEntityReference( item ), group, null );
      return this;
   }

   public LinksBuilder addDescribable( Describable item, String group, String classes )
   {
      addLink( item.getDescription(), EntityReference.getEntityReference( item ), group, classes );
      return this;
   }

   public LinksValue newLinks()
   {
      return linksBuilder.newInstance();
   }
}