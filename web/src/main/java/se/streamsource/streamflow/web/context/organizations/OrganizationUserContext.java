/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Interactions;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.DeleteInteraction;

/**
 * JAVADOC
 */
@Mixins(OrganizationUserContext.Mixin.class)
public interface OrganizationUserContext
   extends DeleteInteraction, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements OrganizationUserContext
   {
      public void delete() throws ResourceException
      {
         Organization org = context.get( Organization.class );
         OrganizationParticipations uop = context.get(OrganizationParticipations.class);

         uop.leave( org );
      }
   }
}
