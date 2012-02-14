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
package se.streamsource.dci.restlet.client.requestwriter;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.RequestWriter;

/**
 * Request writer for ValueComposites. Transfers value state to request reference as query parameters or JSON entity
 */
public class ValueCompositeRequestWriter
   implements RequestWriter
{
   @Structure
   private Qi4jSPI spi;

   public boolean writeRequest(Object requestObject, Request request) throws ResourceException
   {
      if (requestObject instanceof ValueComposite)
      {
         // Value as parameter
         ValueComposite valueObject = (ValueComposite) requestObject;
         if (request.getMethod().equals(Method.GET))
         {
            StateHolder holder = spi.getState( valueObject );
            final ValueDescriptor descriptor = spi.getValueDescriptor( valueObject );

            final Reference ref = request.getResourceRef();
            ref.setQuery( null );

            holder.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
            {
               public void visitProperty( QualifiedName
                     name, Object value )
               {
                  if (value != null)
                  {
                     PropertyTypeDescriptor propertyDesc = descriptor.state().getPropertyByQualifiedName( name );
                     String queryParam = propertyDesc.propertyType().type().toQueryParameter( value );
                     ref.addQueryParameter( name.name(), queryParam );
                  }
               }
            } );
         } else
         {
            request.setEntity(new StringRepresentation( valueObject.toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8 ));
         }

         return true;
      }

      return false;
   }
}
