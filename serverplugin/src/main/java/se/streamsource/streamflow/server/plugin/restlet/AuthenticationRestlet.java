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

package se.streamsource.streamflow.server.plugin.restlet;

import org.json.JSONException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import se.streamsource.streamflow.server.plugin.authentication.Authenticator;

/**
 * Delegate Restlet calls to the Authentication service.
 */
public class AuthenticationRestlet
      extends Restlet
{
   @Optional
   @Service
   Authenticator authenticator;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   private Qi4jSPI spi;

   @Structure
   private ModuleSPI module;

   @Override
   public void handle( Request request, Response response )
   {
      super.handle( request, response );

      try
      {
         if (authenticator == null)
         {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
            return;
         }

         if (request.getMethod().equals( Method.GET ))
         {
            if (request.getChallengeResponse() == null)
            {
               response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST );
            }
            if ("henrik".equals( request.getChallengeResponse().getIdentifier() ))
            {
               response.setStatus( Status.SUCCESS_NO_CONTENT );
            } else
            {
               response.setStatus( Status.CLIENT_ERROR_UNAUTHORIZED );
            }

//            if (request.getResourceRef().getQuery() == null || request.getResourceRef().getQuery().isEmpty())
//            {
//               response.setEntity( new InputRepresentation( getClass().getResourceAsStream( "contactform.html" ) ) );
//               response.setStatus( Status.SUCCESS_OK );
//            } else
//            {
//               // Parse request
//               ContactValue contactTemplate;
//
//               contactTemplate = (ContactValue) getValueFromForm( ContactValue.class, request.getResourceRef().getQueryAsForm() );
//
//               // Call plugin
//               ContactList lookups = authenticator.lookup( contactTemplate );
//
//               // Send response
//               String json = lookups.toJSON();
//
//               StringRepresentation result = new StringRepresentation( json, MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8 );
//               response.setStatus( Status.SUCCESS_OK );
//               response.setEntity( result );
//            }
         } else
         {
            response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
         }
      } finally
      {
         request.release();
      }
   }

   private ValueComposite getValueFromForm( Class<? extends ValueComposite> valueType, final Form asForm )
   {
      ValueBuilder<? extends ValueComposite> builder = vbf.newValueBuilder( valueType );
      final ValueDescriptor descriptor = spi.getValueDescriptor( builder.prototype() );
      builder.withState( new StateHolder()
      {
         public <T> Property<T> getProperty( QualifiedName name )
         {
            return null;
         }

         public <T> Property<T> getProperty( java.lang.reflect.Method propertyMethod )
         {
            return null;
         }

         public <ThrowableType extends Throwable> void visitProperties( StateVisitor<ThrowableType> visitor )
               throws ThrowableType
         {
            for (PropertyType propertyType : descriptor.valueType().types())
            {
               Parameter param = asForm.getFirst( propertyType.qualifiedName().name() );
               if (param != null)
               {
                  String value = param.getValue();
                  if (value == null)
                     value = "";
                  try
                  {
                     Object valueObject = propertyType.type().fromQueryParameter( value, module );
                     visitor.visitProperty( propertyType.qualifiedName(), valueObject );
                  } catch (JSONException e)
                  {
                     throw new IllegalArgumentException( "Query parameter has invalid JSON format", e );
                  }
               }
            }
         }
      } );
      return builder.newInstance();
   }
}
