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
package se.streamsource.streamflow.web.rest.resource.admin;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.Writer;

/**
 * This resource allows the user to follow log messages in realtime. Various filtering can be applied.
 * See log_settings.html for options
 */
public class LoggingServerResource
   extends ServerResource
{
   @Override
   protected Representation get() throws ResourceException
   {
      getResponse().release();

      return new WriterRepresentation( MediaType.TEXT_PLAIN)
      {
         @Override
         public void write( final Writer writer ) throws IOException
         {
            writer.flush();

            final Form params = getRequest().getResourceRef().getQueryAsForm();
            final Logger logger = Logger.getRootLogger();

            AppenderSkeleton appender = new LoggingAppender( params, writer, logger );
            appender.setLayout( new PatternLayout("[%X{url}] %-5p %c{1} : %m%n") );
            logger.addAppender( appender );

            synchronized (appender)
            {
               try
               {
                  appender.wait();
               } catch (InterruptedException e)
               {
               }
            }

            logger.removeAppender( appender );
         }
      };
   }

   private class LoggingAppender extends AppenderSkeleton
   {
      private boolean closed;
      private final Form params;
      private final Writer writer;
      private final Logger logger;

      public LoggingAppender( Form params, Writer writer, Logger logger )
      {
         this.params = params;
         this.writer = writer;
         this.logger = logger;
         closed = false;
      }

      @Override
      protected void append( LoggingEvent event )
      {
         try
         {
            if (!closed)
            {
               for (Parameter param : params)
               {
                  if (param.getValue() != null)
                  {
                     String val = MDC.get( param.getName() );

                     if (param.getName().equals("logger"))
                     {
                        if (!event.getLoggerName().contains( param.getValue() ))
                           return;
                     } else if (param.getName().equals("level"))
                     {
                        if (!event.getLevel().isGreaterOrEqual( Level.toLevel( param.getValue() )))
                           return;
                     }
                     else if (val == null || !param.getValue().equals(val))
                        return;
                  }
               }


               writer.write( layout.format(event ));
               writer.flush();
            }
         } catch (IOException e)
         {
            close();

         }
      }

      public void close()
      {
         closed = true;

         synchronized (this)
         {
            this.notifyAll();
         }
      }

      public boolean requiresLayout()
      {
         return false;
      }
   }
}
