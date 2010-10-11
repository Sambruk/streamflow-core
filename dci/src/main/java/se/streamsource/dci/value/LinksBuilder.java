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

package se.streamsource.dci.value;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Builder for making it easier to create LinksValue/LinkValue
 */
public class LinksBuilder<T extends LinksBuilder>
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

   public T path(@Optional String subPath)
   {
      try
      {
         path = subPath == null ? null : URLEncoder.encode( subPath, "UTF-8");
      } catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }

      return (T) this;
   }

   public T rel(String rel)
   {
      this.rel = rel;

      return (T) this;
   }

   public T command( String commandName )
   {
      this.command = commandName;
      this.rel = commandName;
      return (T) this;
   }

   public T addLink( LinkValue linkValue )
   {
      linksBuilder.prototype().links().get().add( linkValue  );
      return (T) this;
   }

   public T addLink( String description, EntityReference ref )
   {
      return (T) addLink(description, ref.identity());
   }

   public T addLink( String description, String id )
   {
      try
      {
         linkBuilder.prototype().text().set( description );
         linkBuilder.prototype().id().set( id );
         if (command != null)
            linkBuilder.prototype().href().set( command+"?entity="+id );
         else
            linkBuilder.prototype().href().set( (path == null ? "" : path+"/")+URLEncoder.encode( id, "UTF-8")+"/" );
         linkBuilder.prototype().rel().set( rel );

         addLink(linkBuilder.newInstance());

         return (T) this;
      } catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
         return (T) this;
      }
   }

   public T addLink( String description, String id, String rel, String href, String classes )
   {
      linkBuilder.prototype().text().set( description );
      linkBuilder.prototype().id().set( id );
      linkBuilder.prototype().rel().set( rel );
      linkBuilder.prototype().href().set( href );
      linkBuilder.prototype().classes().set( classes );

      addLink(linkBuilder.newInstance());

      return (T) this;
   }

   public T addLink( String description, EntityReference ref, String title, String classes )
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

      titledLinkBuilder.prototype().classes().set( classes );

      linksBuilder.prototype().links().get().add( titledLinkBuilder.newInstance() );

      return (T) this;
   }

   public LinksValue newLinks()
   {
      return linksBuilder.newInstance();
   }
}