/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.bootstrap.Assembler;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.ext.servlet.ServletAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that starts Streamflow plugins and delegates requests to them
 */
public class StreamflowPluginServlet
      extends HttpServlet
{
   /**
    * 
    */
   private static final long serialVersionUID = 3101786052397455154L;
   
   private ServletAdapter adapter;
   public Application application;

   @Override
   public void init() throws ServletException
   {
      try
      {
         String preferenceNode = getInitParameter("preference-node");
         String assemblerClassName = getInitParameter( "assembler" );
         String jmxSuffix = getInitParameter( "name" );

         Class assemblerClass = Thread.currentThread().getContextClassLoader().loadClass( assemblerClassName );

         Assembler pluginAssembler = (Assembler) assemblerClass.newInstance();

         application = new StreamflowPluginRestApplication(new Context(), pluginAssembler, preferenceNode, jmxSuffix);

         application.start();

         adapter = new ServletAdapter(getServletContext(), application);
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
         application.stop();
      } catch (Throwable throwable)
      {
         getServletContext().log( "Could not stop Streamflow plugins", throwable );
      }
   }

   protected void service( HttpServletRequest req, HttpServletResponse res )
         throws ServletException, IOException
   {
      this.adapter.service( req, res );
   }
}
