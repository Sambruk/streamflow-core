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

package se.streamsource.streamflow.web.resource.admin;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import se.streamsource.streamflow.web.infrastructure.index.SolrSearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * JAVADOC
 */
public class SolrSearchServerResource
      extends ServerResource
{
   @Service
   SolrSearch search;

   @Structure
   ValueBuilderFactory vbf;

   public SolrSearchServerResource()
   {
      getVariants().addAll( Arrays.asList( new Variant( MediaType.TEXT_HTML ) ) );
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      if (getRequest().getResourceRef().getQueryAsForm().getFirst( "help" ) != null)
      {
         return new InputRepresentation( getClass().getResourceAsStream( "searchhelp.html" ), MediaType.TEXT_HTML );
      } else
      {
         try
         {
            String template = TemplateUtil.getTemplate( "search.html",
                  ConsoleServerResource.class );
            String content = TemplateUtil.eval( template,
                  "$search", "",
                  "$out", "" );
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

      String searchString = form.getFirstValue( "search" );

      try
      {
         SolrDocumentList result = search.search( searchString );


         String resultString = "<pre>";

         for (SolrDocument solrDocument : result)
         {
            for (Map.Entry<String, Object> stringObjectEntry : solrDocument)
            {
               resultString += stringObjectEntry.getKey() + "=" + stringObjectEntry.getValue() + "\n";
            }
            resultString += "\n";
         }
         resultString += "</pre>";

         String template = TemplateUtil.getTemplate( "search.html",
               ConsoleServerResource.class );
         String content = TemplateUtil.eval( template,
               "$search", searchString,
               "$out", resultString );
         return new StringRepresentation( content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8 );
      } catch (Exception e)
      {
         StringWriter out = new StringWriter();
         e.printStackTrace( new PrintWriter( out ) );

         try
         {
            String template = TemplateUtil.getTemplate( "search.html",
                  ConsoleServerResource.class );
            String content = TemplateUtil.eval( template,
                  "$search", searchString,
                  "$out", out.toString() );
            return new StringRepresentation( content, MediaType.TEXT_HTML, null, CharacterSet.UTF_8 );
         } catch (IOException e1)
         {
            throw new ResourceException( e1 );
         }
      }
   }
}
