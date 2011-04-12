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

package se.streamsource.streamflow.infrastructure.event.application.factory;

import org.json.*;
import org.qi4j.api.concern.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.infrastructure.event.application.*;
import se.streamsource.streamflow.infrastructure.time.*;

/**
 * DomainEvent factory
 */
@Concerns(TransactionNotificationConcern.class)
@Mixins(ApplicationEventFactoryService.Mixin.class)
public interface ApplicationEventFactoryService
      extends ApplicationEventFactory, ServiceComposite
{
   class Mixin
         implements ApplicationEventFactory
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      IdentityGenerator idGenerator;

      @Service
      Time time;

      String version;

      public void init( @Structure Application application )
      {
         version = application.version();
      }

      public ApplicationEvent createEvent( String name, Object[] args )
      {
         ValueBuilder<ApplicationEvent> builder = vbf.newValueBuilder( ApplicationEvent.class );

         ApplicationEvent prototype = builder.prototype();
         prototype.name().set( name );
         prototype.on().set( time.dateNow() );

         prototype.identity().set( idGenerator.generate( ApplicationEvent.class ) );

         UnitOfWork uow = uowf.currentUnitOfWork();
         prototype.usecase().set( uow.usecase().name() );
         prototype.version().set( version );

         // JSON-ify parameters
         JSONStringer json = new JSONStringer();
         try
         {
            JSONWriter params = json.object();
            for (int i = 1; i < args.length; i++)
            {
               params.key( "param" + i );
               if (args == null)
                  params.value( JSONObject.NULL );
               else
                  params.value( args[i] );
            }
            json.endObject();
         } catch (JSONException e)
         {
            throw new IllegalArgumentException( "Could not create event", e );
         }

         prototype.parameters().set( json.toString() );

         ApplicationEvent event = builder.newInstance();

         return event;
      }
   }
}
