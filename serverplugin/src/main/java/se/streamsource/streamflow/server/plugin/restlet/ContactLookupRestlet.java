/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.StringRepresentation;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;

/**
 * Delegate Restlet calls to the ContactLookup service.
 */
public class ContactLookupRestlet
      extends Restlet
{
   @Optional
   @Service
   ContactLookup contactLookup;

   @Structure
   ValueBuilderFactory vbf;

   @Override
   public void handle( Request request, Response response )
   {
      super.handle( request, response );

      if (contactLookup == null)
      {
         response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
         return;
      }

      if (request.getMethod().equals( Method.GET ))
      {
         response.setEntity(new InputRepresentation(getClass().getResourceAsStream( "contactform.html" )));
         response.setStatus( Status.SUCCESS_OK );
      } else if (request.getMethod().equals( Method.POST ))
      {
         // Parse request
         ContactValue contactTemplate;

         if (request.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON ))
         {
            contactTemplate = vbf.newValueFromJSON( ContactValue.class, request.getEntityAsText() );
         } else
         {
            contactTemplate = vbf.newValueFromJSON( ContactValue.class, new Form(request.getEntity()).getFirstValue( "template" ));
         }

         // Call plugin
         ContactList lookups = contactLookup.lookup( contactTemplate );

         // Send response
         String json = lookups.toJSON();

         StringRepresentation result = new StringRepresentation( json, MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8 );
         response.setStatus( Status.SUCCESS_OK );
         response.setEntity( result );
      } else
      {
         response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
      }
   }
}
