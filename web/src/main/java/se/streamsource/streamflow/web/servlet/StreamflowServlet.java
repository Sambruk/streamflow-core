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

import org.restlet.ext.servlet.ServletAdapter;
import se.streamsource.streamflow.web.MainWeb;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that starts Streamflow and delegates requests to it
 */
public class StreamflowServlet
      extends HttpServlet
{
   private MainWeb mainWeb;
   private ServletAdapter adapter;

   @Override
   public void init() throws ServletException
   {
      mainWeb = new MainWeb();

      try
      {
         mainWeb.start();

         adapter = new ServletAdapter(getServletContext(), mainWeb.getApplication());
      } catch (Throwable throwable)
      {
         throw new ServletException(throwable);
      }
   }

   @Override
   public void destroy()
   {
      try
      {
         mainWeb.stop();
      } catch (Throwable throwable)
      {
         getServletContext().log( "Could not stop Streamflow", throwable );
      }
   }

   protected void service( HttpServletRequest req, HttpServletResponse res )
         throws ServletException, IOException
   {
      this.adapter.service( req, res );
   }
}
