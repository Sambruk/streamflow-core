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

package se.streamsource.streamflow.web.activator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.restlet.Restlet;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.MainWeb;

import java.util.Dictionary;
import java.util.Properties;

/**
 * Activator for Streamflow. Start Streamflow application and register as Restlet service
 * in OSGi.
 */
public class StreamflowActivator implements BundleActivator
{
   public MainWeb mainWeb;
   public ServiceRegistration reg;

   public void start( BundleContext context ) throws Exception
   {
      mainWeb = new MainWeb();
      mainWeb.start();

      reg = context.registerService( Restlet.class.getName(), mainWeb.getApplication(), new Properties() );
   }

   public void stop( BundleContext context ) throws Exception
   {
      reg.unregister();

      mainWeb.stop();
   }
}