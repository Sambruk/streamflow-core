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

package se.streamsource.streamflow.web.resource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

/**
 * This filter will record all the Events from the call, and if any occured and the
 * result entity is EmptyRepresentation, will return these events as the result.
 */
public class EventsFilter
      extends Filter
      implements TransactionVisitor
{
   @Structure
   protected ValueBuilderFactory vbf;

   public ThreadLocal<TransactionEvents> transactions = new ThreadLocal<TransactionEvents>();

   private Template eventsTemplate;

   public EventsFilter( @Uses Context context, @Uses Restlet next, @Service EventSource source, @Service VelocityEngine templates) throws Exception
   {
      super( context, next );

      source.registerListener( this );

      eventsTemplate = templates.getTemplate( "se/streamsource/streamflow/web/resource/resources/events.html" );
   }

   @Override
   protected int doHandle( Request request, Response response )
   {
      Method method = request.getMethod();
      if ((method.equals( Method.POST ) || method.equals( Method.PUT ) || method.equals( Method.DELETE )))
      {

         int result;
         transactions.set( null );
         result = super.doHandle( request, response );

         if (response.getStatus().equals( Status.INFO_CONTINUE) && response.getEntity() instanceof EmptyRepresentation)
         {
            MediaType responseType = request.getClientInfo().getPreferredMediaType( Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML ) );
            final EventFilter filter = new EventFilter( AllEventsSpecification.INSTANCE );
            Representation rep;
            if (responseType == null || (responseType.equals( MediaType.TEXT_PLAIN )))
            {
               rep = new WriterRepresentation( MediaType.TEXT_PLAIN )
               {
                  public void write( Writer writer ) throws IOException
                  {
                     if (transactions == null)
                     {
                        ValueBuilder<TransactionEvents> builder = vbf.newValueBuilder( TransactionEvents.class );
                        builder.prototype().timestamp().set( System.currentTimeMillis() );
                        TransactionEvents events = builder.newInstance();
                        writer.write( events.toJSON() );
                     } else
                     {
                        TransactionEvents events = transactions.get();
                        writer.write( events.toJSON() );
                     }
                  }
               };
            } else if (responseType.equals( MediaType.TEXT_HTML ))
            {
               rep = new WriterRepresentation( MediaType.TEXT_HTML )
               {
                  public void write( Writer writer ) throws IOException
                  {
                     StringWriter string = new StringWriter();
                     for (DomainEvent event : filter.events( Collections.singletonList( transactions.get()) ))
                     {
                        string.write( "<tr>" +
                              "<td>" + event.usecase().get() + "</td>" +
                              "<td>" + event.name().get() + "</td>" +
                              "<td>" + event.on().get() + "</td>" +
                              "<td>" + event.entity().get() + "</td>" +
                              "<td>" + event.parameters().get() + "</td>" +
                              "<td>" + event.by().get() + "</td></tr>" );
                     }

                     VelocityContext context = new VelocityContext();
                     context.put("events", string.toString());
                     eventsTemplate.merge( context, writer );;
                  }
               };
            } else
            {
               rep = new WriterRepresentation( MediaType.APPLICATION_JSON )
               {
                  public void write( Writer writer ) throws IOException
                  {
                     writer.write( transactions.get().toJSON() );
                  }
               };
            }

            response.setStatus( Status.SUCCESS_OK );
            response.setEntity( rep );
         }

         return result;
      } else
      {
         return super.doHandle( request, response );
      }
   }

   public boolean visit( TransactionEvents transaction )
   {
      transactions.set(transaction );

      return true;
   }
}