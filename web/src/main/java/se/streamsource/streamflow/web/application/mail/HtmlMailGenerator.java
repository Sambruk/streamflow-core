/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.application.mail;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.util.Strings;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Factory for creating html formatted mail content.
 */

public class HtmlMailGenerator
{
   @Service
   VelocityEngine velocity;

   public String createMailContent( String content, String footer )
   {
      VelocityContext context = new VelocityContext();
      context.put( "content", content );
      context.put( "footer", Strings.empty( footer ) ? "" : footer );

      return createFromFile( "se/streamsource/streamflow/web/application/mail/htmlmail_sv.html", context );
   }

   public String createDoubleSignatureMail( String templateString, VelocityContext context )
   {
      return createFromString( templateString , context );
   }

   public String createDueOnNotificationMail( VelocityContext context )
   {
      return createFromFile( "se/streamsource/streamflow/web/application/mail/dueonnotificationhtmlmail_sv.html" , context );
   }

   private String createFromFile( String templateFile, VelocityContext context )
   {
      StringWriter result = new StringWriter(  );
      try
      {
         Template template = velocity.getTemplate( templateFile );
         template.merge( context, result );

      } catch( Throwable throwable )
      {
         throw new IllegalArgumentException( "Could not create html mail.", throwable );
      }
      return result.toString();
   }

   private String createFromString( String template, VelocityContext context )
   {
      StringWriter result = new StringWriter( );
      try
      {
         velocity.evaluate( context, result, "", new StringReader( template ) );
      }  catch( Throwable throwable )
      {
         throw new IllegalArgumentException( "Could not create html mail.", throwable );
      }
      return result.toString();
   }
}
