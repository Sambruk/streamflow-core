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

package se.streamsource.dci.value;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * Builder for making it easier to create LinksValue/LinkValue
 */
public class LinksBuilder
{
   protected ValueBuilder<? extends LinksValue> linksBuilder;
   protected ValueBuilder<LinkValue> linkBuilder;
   protected ValueBuilder<TitledLinkValue> titledLinkBuilder;
   protected ValueBuilderFactory vbf;

   private String path;
   private String rel;
   private String command;

   public LinksBuilder( ValueBuilderFactory vbf )
   {
      this.vbf = vbf;
      linksBuilder = vbf.newValueBuilder( LinksValue.class );
      linkBuilder = vbf.newValueBuilder( LinkValue.class );
   }

   public LinksBuilder path(String subPath)
   {
      path = subPath;

      return this;
   }

   public LinksBuilder rel(String rel)
   {
      this.rel = rel;

      return this;
   }

   public LinksBuilder command( String commandName )
   {
      this.command = commandName;
      this.rel = commandName;
      return this;
   }

   public LinksBuilder addLink( LinkValue linkValue )
   {
      linksBuilder.prototype().links().get().add( linkValue  );
      return this;
   }

   public LinksBuilder addLink( String description, EntityReference ref )
   {
      return addLink(description, ref.identity());
   }

   public LinksBuilder addLink( String description, String id )
   {
      linkBuilder.prototype().text().set( description );
      linkBuilder.prototype().id().set( id );
      if (command != null)
         linkBuilder.prototype().href().set( command+"?entity="+id );
      else
         linkBuilder.prototype().href().set( (path == null ? "" : path+"/")+id+"/" );
      linkBuilder.prototype().rel().set( rel );

      addLink(linkBuilder.newInstance());

      return this;
   }

   public LinksBuilder addLink( String description, String id, String rel, String href )
   {
      linkBuilder.prototype().text().set( description );
      linkBuilder.prototype().id().set( id );
      linkBuilder.prototype().rel().set( rel );
      linkBuilder.prototype().href().set( href );

      addLink(linkBuilder.newInstance());

      return this;
   }

   public LinksBuilder addLink( String description, EntityReference ref, String title )
   {
      if (titledLinkBuilder == null)
         titledLinkBuilder = vbf.newValueBuilder( TitledLinkValue.class );

      titledLinkBuilder.prototype().text().set( description );
      titledLinkBuilder.prototype().id().set( ref.identity() );

      if (command != null)
         titledLinkBuilder.prototype().href().set( command+"?entity="+ref.identity() );
      else
         titledLinkBuilder.prototype().href().set( (path == null ? "" : path+"/")+ref.identity()+"/" );
      titledLinkBuilder.prototype().rel().set( rel );

      titledLinkBuilder.prototype().title().set( title );

      linksBuilder.prototype().links().get().add( titledLinkBuilder.newInstance() );

      return this;
   }

   public LinksValue newLinks()
   {
      return linksBuilder.newInstance();
   }
}