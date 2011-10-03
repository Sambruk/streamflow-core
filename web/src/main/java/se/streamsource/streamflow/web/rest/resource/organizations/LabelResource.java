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

package se.streamsource.streamflow.web.rest.resource.organizations;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.labels.LabelContext;
import se.streamsource.streamflow.web.context.administration.labels.SelectedLabelContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
public class LabelResource
   extends CommandQueryResource
   implements SubResources
{
   public LabelResource()
   {
      super( LabelContext.class, DescribableContext.class );
   }

   public LinksValue possiblemoveto() throws Throwable
   {
      Iterable labels = context(LabelContext.class).possiblemoveto();
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );

      builder.addDescribables( labels );

      return builder.newLinks();

   }

   public void resource( String segment ) throws ResourceException
   {
      setRole( SelectedLabels.class, segment );
      subResourceContexts( SelectedLabelContext.class );
   }
}
