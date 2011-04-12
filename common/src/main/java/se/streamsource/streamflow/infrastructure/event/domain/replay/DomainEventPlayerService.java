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

package se.streamsource.streamflow.infrastructure.event.domain.replay;

import org.json.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.*;
import org.qi4j.spi.entity.*;
import org.slf4j.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;

import java.lang.reflect.*;
import java.text.*;
import java.util.*;

/**
 * DomainEvent player
 */
@Mixins(DomainEventPlayerService.Mixin.class)
public interface DomainEventPlayerService
      extends DomainEventPlayer, ServiceComposite
{
   class Mixin
         implements DomainEventPlayer
   {
      final Logger logger = LoggerFactory.getLogger( DomainEventPlayer.class.getName() );

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      Module module;

      @Structure
      Qi4jSPI spi;

      SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss zzz yyyy" );

      public void playTransaction( TransactionDomainEvents transactionDomain )
            throws EventReplayException
      {
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Event replay" ) );
         DomainEvent currentEvent = null;
         try
         {
            for (DomainEvent domainEvent : transactionDomain.events().get())
            {
               currentEvent = domainEvent;
               // Get the entity
               Class entityType = module.classLoader().loadClass( domainEvent.entityType().get() );
               String id = domainEvent.entity().get();
               Object entity = uow.get( entityType, id );

               // check if the event has already occured
               EntityState state = spi.getEntityState( (EntityComposite) entity );
               if (state.lastModified() > currentEvent.on().get().getTime())
               {
                  break; // don't rerun event in this transactionDomain
               }

               playEvent( domainEvent, entity );
            }
            uow.complete();
         } catch (Exception e)
         {
            uow.discard();
            if (e instanceof EventReplayException)
               throw ((EventReplayException) e);
            else
               throw new EventReplayException( currentEvent, e );
         }
      }

      public void playEvent( DomainEvent domainEvent, Object object )
            throws EventReplayException
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         Class entityType = object.getClass();

         // Get method
         Method eventMethod = getEventMethod( entityType, domainEvent.name().get() );

         if (eventMethod == null)
         {
            logger.warn( "Could not find event method " + domainEvent.name().get() + " in entity of type " + entityType.getName() );
            return;
         }

         // Build parameters
         try
         {
            String jsonParameters = domainEvent.parameters().get();
            JSONObject parameters = (JSONObject) new JSONTokener( jsonParameters ).nextValue();
            Object[] args = new Object[eventMethod.getParameterTypes().length];
            for (int i = 1; i < eventMethod.getParameterTypes().length; i++)
            {
               Class<?> parameterType = eventMethod.getParameterTypes()[i];

               String paramName = "param" + i;

               Object value = parameters.get( paramName );

               args[i] = getParameterArgument( parameterType, value, uow );
            }

            args[0] = domainEvent;

            // Invoke method
            logger.debug( "Replay:" + domainEvent + " on:" + object );

            eventMethod.invoke( object, args );
         } catch (Exception e)
         {
            throw new EventReplayException( domainEvent, e );
         }
      }

      private Object getParameterArgument( Class<?> parameterType, Object value, UnitOfWork uow ) throws ParseException
      {
         if (value.equals( JSONObject.NULL ))
            return null;

         if (parameterType.equals( String.class ))
         {
            return (String) value;
         } else if (parameterType.equals( Boolean.class ) || parameterType.equals( Boolean.TYPE ))
         {
            return (Boolean) value;
         } else if (parameterType.equals( Long.class ) || parameterType.equals( Long.TYPE ))
         {
            return ((Number) value).longValue();
         } else if (parameterType.equals( Integer.class ) || parameterType.equals( Integer.TYPE ))
         {
            return ((Number) value).intValue();
         } else if (parameterType.equals( Date.class ))
         {
            return dateFormat.parse( (String) value );
         } else if (ValueComposite.class.isAssignableFrom( parameterType ))
         {

            return module.valueBuilderFactory().newValueFromJSON( parameterType, (String) value );
         } else if (parameterType.isInterface())
         {
            return uow.get( parameterType, (String) value );
         } else if (parameterType.isEnum())
         {
            return Enum.valueOf( (Class<? extends Enum>) parameterType, value.toString() );
         } else
         {
            throw new IllegalArgumentException( "Unknown parameter type:" + parameterType.getName() );
         }
      }

      private Method getEventMethod( Class<?> aClass, String eventName )
      {
         for (Method method : aClass.getMethods())
         {
            if (method.getName().equals( eventName ))
            {
               Class[] parameterTypes = method.getParameterTypes();
               if (parameterTypes.length > 0 && parameterTypes[0].equals( DomainEvent.class ))
                  return method;
            }
         }
         return null;
      }
   }
}