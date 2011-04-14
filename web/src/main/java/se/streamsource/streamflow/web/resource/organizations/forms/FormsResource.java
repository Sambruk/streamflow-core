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

package se.streamsource.streamflow.web.resource.organizations.forms;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.forms.FormsContext;
import se.streamsource.streamflow.web.domain.structure.form.Forms;

/**
 * JAVADOC
 */
public class FormsResource
      extends CommandQueryResource
      implements SubResources
{
   public FormsResource()
   {
      super( FormsContext.class );
   }

   public void possiblemoveto() throws Throwable
   {

      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );
      Iterable<Describable> forms = (Iterable<Describable>) invoke();

      builder.addDescribables( forms );

      result(builder.newLinks());
   }

   public void resource( String segment ) throws ResourceException
   {
      findManyAssociation( RoleMap.role( Forms.Data.class).forms(), segment );
      subResource( FormResource.class );
   }
}
