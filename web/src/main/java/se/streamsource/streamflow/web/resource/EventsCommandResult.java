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
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.routing.Filter;
import se.streamsource.dci.restlet.server.CommandResult;
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
public class EventsCommandResult
      implements CommandResult, TransactionVisitor
{
   @Structure
   protected ValueBuilderFactory vbf;

   public ThreadLocal<TransactionEvents> transactions = new ThreadLocal<TransactionEvents>();

   public EventsCommandResult( @Service EventSource source) throws Exception
   {
      source.registerListener( this );
   }

   public Object getResult()
   {
      TransactionEvents tx = transactions.get();
      transactions.set(null);
      if (tx == null)
      {
         ValueBuilder<TransactionEvents> builder = vbf.newValueBuilder( TransactionEvents.class );
         builder.prototype().timestamp().set( System.currentTimeMillis() );
         tx = builder.newInstance();
      }

      return tx;
   }


   public boolean visit( TransactionEvents transaction )
   {
      transactions.set(transaction );

      return true;
   }
}