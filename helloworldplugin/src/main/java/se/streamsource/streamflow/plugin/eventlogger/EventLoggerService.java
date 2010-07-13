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

package se.streamsource.streamflow.plugin.eventlogger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Verifier;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

/**
 * JAVADOC
 */
public class EventLoggerService
   implements BundleActivator, ServiceTrackerCustomizer, TransactionVisitor, Verifier
{
   private BundleContext bundleContext;
   public EventSource source;
   public ServiceTracker eventSource;

   public void start( BundleContext bundleContext ) throws Exception
   {
      this.bundleContext = bundleContext;
      eventSource = new ServiceTracker(bundleContext, EventSource.class.getName(), this);
      eventSource.open();

      
   }

   public void stop( BundleContext bundleContext ) throws Exception
   {
      eventSource.close();
   }

   public Object addingService( ServiceReference reference )
   {
      source = (EventSource) bundleContext.getService( reference );
      source.registerListener( this );

      return source;
   }

   public void modifiedService( ServiceReference reference, Object service )
   {
   }

   public void removedService( ServiceReference reference, Object service )
   {
      EventSource source = (EventSource) service;
      source.unregisterListener( this );
   }

   public boolean visit( TransactionEvents transaction )
   {
      for (DomainEvent domainEvent : transaction.events().get())
      {
         LoggerFactory.getLogger( "events" ).info( domainEvent.name().get() );
      }

      return true;
   }

   public int verify( Request request, Response response )
   {
      return 0;
   }
}
