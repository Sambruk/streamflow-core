/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource.surface.administration.organizations.emailaccesspoints;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints.EmailAccessPointAdministrationContext;
import se.streamsource.streamflow.web.rest.resource.surface.administration.organizations.accesspoints.AccessPointLabelableResource;

/**
 * TODO
 */
public class EmailAccessPointAdministrationResource
   extends CommandQueryResource
{
   public EmailAccessPointAdministrationResource()
   {
      super(EmailAccessPointAdministrationContext.class);
   }

   public LinksValue possibleprojects() throws Throwable
   {
      return new LinksBuilder(module.valueBuilderFactory()).
            command( "changeproject" ).
            addDescribables( context(EmailAccessPointAdministrationContext.class).possibleprojects() ).
            newLinks();
   }

   public LinksValue possiblecasetypes() throws Throwable
   {
      return new LinksBuilder(module.valueBuilderFactory()).
            command( "changecasetype" ).
            addDescribables( context(EmailAccessPointAdministrationContext.class).possiblecasetypes() ).
            newLinks();
   }

   @SubResource
   public void labels()
   {
      subResource( AccessPointLabelableResource.class );
   }
}
