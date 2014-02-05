/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource;

import java.text.DateFormat;
import java.util.Date;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Log command and query performance
*/
class PerformanceLoggingFilter extends Filter
{
   Logger queryPerformanceMonitor;
   Logger commandPerformanceMonitor;

   public PerformanceLoggingFilter( Context context, Restlet restlet )
   {
      super( context, restlet );
      queryPerformanceMonitor = LoggerFactory.getLogger( "monitor.rest.query" );
      commandPerformanceMonitor = LoggerFactory.getLogger( "monitor.rest.command" );
   }

   @Override
   protected int doHandle( Request request, Response response )
   {
      long start = System.nanoTime();
      try
      {
         return super.doHandle( request, response );
      } finally
      {
         long end = System.nanoTime();
         long requestTime = (end - start) / 1000000L;
         Date now = new Date();

         if (request.getMethod().equals( Method.GET ))
         {
            queryPerformanceMonitor.info( "{}\t{}\t{}\t{}", new Object[]{requestTime, DateFormat.getDateTimeInstance().format( now ), request.getResourceRef().getLastSegment(), request.getResourceRef()} );
         } else
         {
            commandPerformanceMonitor.info( "{}\t{}\t{}\t{}", new Object[]{requestTime, DateFormat.getDateTimeInstance().format( now ), request.getResourceRef().getLastSegment(), request.getResourceRef()} );
         }
      }
   }
}
