/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.resource.admin;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.streamflow.web.application.console.Console;
import se.streamsource.streamflow.web.application.console.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * JAVADOC
 */
public class ConsoleServerResource
      extends ServerResource
{
   @Service
   Console console;

   @Structure
   ValueBuilderFactory vbf;

   public ConsoleServerResource()
   {
      getVariants().addAll( Arrays.asList( new Variant( MediaType.TEXT_HTML ) ) );
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      if (getRequest().getResourceRef().getQueryAsForm().getFirst( "help" ) != null)
      {
         return new InputRepresentation( getClass().getResourceAsStream( "help.html" ), MediaType.TEXT_HTML );
      } else
      {
         try
         {
            String template = TemplateUtil.getTemplate( "console.html",
                  ConsoleServerResource.class );
            String content = TemplateUtil.eval( template,
                  "$script", "",
                  "$out", "",
                  "$log", "" );
            return new StringRepresentation( content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8 );
         } catch (IOException e)
         {
            throw new ResourceException( e );
         }
      }
   }

   @Override
   protected Representation post( Representation representation, Variant variant ) throws ResourceException
   {
      Form form = new Form( representation );

      String script = form.getFirstValue( "script" );
      ValueBuilder<ConsoleScriptValue> builder = vbf.newValueBuilder( ConsoleScriptValue.class );
      builder.prototype().script().set( script );
      String firstValue = form.getFirstValue( "complete" );
      builder.prototype().completeUnitOfWork().set( firstValue.equals( "yes" ) );

      try
      {
         ConsoleResultValue result = console.executeScript( builder.newInstance() );

         String log = "";
         SimpleFormatter formatter = new SimpleFormatter();
         for (LogRecord logRecord : result.log().get())
         {
            log += formatter.format( logRecord ) + "\n";
         }

         String template = TemplateUtil.getTemplate( "console.html",
               ConsoleServerResource.class );
         String content = TemplateUtil.eval( template,
               "$script", script,
               "$out", result.out().get(),
               "$log", log );
         return new StringRepresentation( content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8 );
      } catch (Exception e)
      {
         StringWriter out = new StringWriter();
         e.printStackTrace( new PrintWriter( out ) );

         try
         {
            String template = TemplateUtil.getTemplate( "console.html",
                  ConsoleServerResource.class );
            String content = TemplateUtil.eval( template,
                  "$script", script,
                  "$out", out.toString() );
            return new StringRepresentation( content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8 );
         } catch (IOException e1)
         {
            throw new ResourceException( e1 );
         }
      }
   }
}
