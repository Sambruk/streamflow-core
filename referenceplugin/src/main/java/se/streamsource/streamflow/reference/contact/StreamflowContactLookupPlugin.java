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

package se.streamsource.streamflow.reference.contact;

import org.qi4j.api.common.*;
import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.api.service.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.*;
import org.qi4j.spi.property.*;
import org.qi4j.spi.value.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.streamflow.server.plugin.contact.*;


@Mixins(StreamflowContactLookupPlugin.Mixin.class)
public interface StreamflowContactLookupPlugin
      extends ServiceComposite, ContactLookup, Activatable, Configuration
{

   abstract class Mixin implements StreamflowContactLookupPlugin
   {

      @This
      Configuration<StreamflowContactLookupPluginConfiguration> config;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      private Qi4jSPI spi;

      public void activate() throws Exception
      {
         config.configuration().url().get();
      }
      
      public ContactList lookup( ContactValue contactTemplate )
      {
         try
         {
            ClientResource clientResource = new ClientResource( config.configuration().url().get() );

            clientResource.setChallengeResponse( ChallengeScheme.HTTP_BASIC, config.configuration().accountname().get(), config.configuration().password().get() );

            // Call plugin
            setQueryParameters( clientResource.getReference(), contactTemplate );
            Representation result = clientResource.get( MediaType.APPLICATION_JSON );

            // Parse response
            String json = result.getText();
            return vbf.newValueFromJSON( ContactList.class, json );
         } catch (Exception e)
         {

            // Return empty list
            return vbf.newValue( ContactList.class );
         }
      }

      private void setQueryParameters( final Reference ref, ValueComposite queryValue )
      {
         // Value as parameter
         StateHolder holder = spi.getState( queryValue );
         final ValueDescriptor descriptor = spi.getValueDescriptor( queryValue );

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
      }

   }
}
