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

package se.streamsource.streamflow.server.plugin.restlet;

import org.json.*;
import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.property.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.*;
import org.qi4j.spi.property.*;
import org.qi4j.spi.structure.*;
import org.qi4j.spi.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import se.streamsource.streamflow.server.plugin.contact.*;

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
         if (contactLookup == null)
         {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
            return;
         }

         if (request.getMethod().equals( Method.GET ))
         {
            if (request.getResourceRef().getQuery() == null || request.getResourceRef().getQuery().isEmpty())
            {
               response.setEntity( new InputRepresentation( getClass().getResourceAsStream( "contactform.html" ) ) );
               response.setStatus( Status.SUCCESS_OK );
            } else
            {
               // Parse request
               ContactValue contactTemplate;

               contactTemplate = (ContactValue) getValueFromForm( ContactValue.class, request.getResourceRef().getQueryAsForm() );

               // Call plugin
               ContactList lookups = contactLookup.lookup( contactTemplate );

               // Send response
               String json = lookups.toJSON();

               StringRepresentation result = new StringRepresentation( json, MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8 );
               response.setStatus( Status.SUCCESS_OK );
               response.setEntity( result );
            }
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
