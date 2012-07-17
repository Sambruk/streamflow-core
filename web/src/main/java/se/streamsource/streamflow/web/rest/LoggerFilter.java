/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.rest;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;
import org.slf4j.LoggerFactory;

/**
 * This class is here to catch and log any exceptions that the application can't handle.
 * 
 * @author henrikreinhold
 *
 */
public class LoggerFilter extends Filter
{

   @Override
   protected int doHandle(Request request, Response response)
   {
      try
      {
         return super.doHandle(request, response);
      } catch (Throwable t)
      {
         LoggerFactory.getLogger(getClass()).error("Unhandled exception occured:", t);
         return STOP;
      }
   }

}
