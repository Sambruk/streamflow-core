/*
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

package se.streamsource.streamflow.web.resource.surface.administration.organizations.accesspoints;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.SubResource;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.surface.SelectedTemplateContext;
import se.streamsource.streamflow.web.context.administration.surface.accesspoints.AccessPointAdministrationContext;

/**
 * JAVADOC
 */
public class AccessPointAdministrationResource
      extends CommandQueryResource
{
   public AccessPointAdministrationResource()
   {
      super( AccessPointAdministrationContext.class );
   }

   public void possibleprojects() throws Throwable
   {
      result(new LinksBuilder(module.valueBuilderFactory()).
            command( "setproject" ).
            addDescribables( (Iterable<? extends Describable>) invoke() ).
            newLinks());
   }

   @SubResource
   public void labels()
   {
      subResource( AccessPointLabelableResource.class );
   }

   @SubResource
   public void template()
   {
      subResourceContexts( SelectedTemplateContext.class );
   }
}