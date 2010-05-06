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

package se.streamsource.streamflow.web.context.access.accesspoints.endusers;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.domain.entity.user.AnonymousEndUserEntity;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;
import se.streamsource.streamflow.web.domain.structure.user.EndUsers;

/**
 * JAVADOC
 */
@Mixins(EndUsersContext.Mixin.class)
public interface EndUsersContext
      extends SubContexts<EndUserContext>, Interactions
{
   // command
   void selectenduser( Response response ) throws ResourceException;

   LinkValue viewenduser( Response response ) throws ResourceException;

   abstract class Mixin
         extends InteractionsMixin
         implements EndUsersContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public EndUserContext context( String id)
      {
         AnonymousEndUser endUser = module.unitOfWorkFactory().currentUnitOfWork().get( AnonymousEndUser.class, id );

         context.set( endUser );
         return subContext( EndUserContext.class );
      }

      public void selectenduser( Response response ) throws ResourceException
      {
         // for now only user the anonymous end users
         Series<Cookie> cookies = response.getRequest().getCookies();

         Cookie cookie = findCookie( cookies );
         if ( cookie != null )
         {
            try
            {
               module.unitOfWorkFactory().currentUnitOfWork().get( AnonymousEndUser.class, cookie.getValue() );
            } catch (NoSuchEntityException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
         } else
         {
            AnonymousEndUser anonymousEndUser = createenduser();
            EntityReference entityReference = EntityReference.getEntityReference( anonymousEndUser );

            CookieSetting cookieSetting = new CookieSetting( AnonymousEndUserEntity.COOKIE_NAME, entityReference.identity() );
            // two weeks
            //cookieSetting.setMaxAge( 60 * 60 * 24 * 14 );
            cookieSetting.setMaxAge( 30 );
            response.getCookieSettings().add( cookieSetting );
         }
      }

      private Cookie findCookie( Series<Cookie> cookies )
      {
         for (Cookie cookie : cookies)
         {
            if (cookie.getName().equals( AnonymousEndUserEntity.COOKIE_NAME ) )
            {
               return cookie;
            }
         }
         return null;
      }

      private AnonymousEndUser createenduser( )
      {
         EndUsers endUsers = context.get( EndUsers.class );
         return endUsers.createAnonymousEndUser();
      }

      public LinkValue viewenduser( Response response ) throws ResourceException
      {
         Series<Cookie> cookies = response.getRequest().getCookies();

         Cookie cookie = findCookie( cookies );

         if ( cookie == null )
         {
            throw new ResourceException( Status.CLIENT_ERROR_UNAUTHORIZED );
         }

         try
         {
            AnonymousEndUser endUser = module.unitOfWorkFactory().currentUnitOfWork().get( AnonymousEndUser.class, cookie.getValue() );
            ValueBuilder<LinkValue> builder = vbf.newValueBuilder( LinkValue.class );
            EntityReference entityReference = EntityReference.getEntityReference( endUser );
            builder.prototype().id().set( entityReference.identity() );
            builder.prototype().href().set( entityReference.identity() );
            builder.prototype().text().set( "ANONYMOUS" );
            return builder.newInstance();

         } catch (NoSuchEntityException e)
         {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
         }
      }
   }
}