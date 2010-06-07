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

package se.streamsource.streamflow.web.servlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.restlet.Restlet;
import org.restlet.ext.servlet.ServletAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that looks up Streamflow Restlet in OSGi and delegates to it
 */
public class StreamflowServlet
      extends HttpServlet
{
   private ServletAdapter adapter;
   private ServiceTracker tracker;
   private BundleContext ctx;
   private Logger logger = LoggerFactory.getLogger( getClass() );

   @Override
   public void init() throws ServletException
   {
      super.init();
      this.adapter = new ServletAdapter( getServletContext() );
      ctx = BundleReference.class.cast( StreamflowServlet.class.getClassLoader() ).getBundle().getBundleContext();
      tracker = new ServiceTracker( ctx, Restlet.class.getName(), null)
      {
         @Override
         public Object addingService( ServiceReference reference )
         {
            adapter.setNext( (Restlet) ctx.getService( reference) );
            logger.info( "Streamflow application is online" );
            return super.addingService( reference );
         }

         @Override
         public void modifiedService( ServiceReference reference, Object service )
         {
            adapter.setNext( (Restlet) ctx.getService( reference) );
            super.modifiedService( reference, service );
         }

         @Override
         public void removedService( ServiceReference reference, Object service )
         {
            adapter.setNext( null );
            logger.info( "Streamflow application is offline" );
            super.removedService( reference, service );
         }
      };
      tracker.open();
   }

   @Override
   public void destroy()
   {
      tracker.close();
      super.destroy();
   }

   protected void service( HttpServletRequest req, HttpServletResponse res )
         throws ServletException, IOException
   {
      if (adapter.getNext() == null)
      {
         res.setStatus( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
         return;
      }

      this.adapter.service( req, res );
   }
}
