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

package se.streamsource.streamflow.web.resource.organizations;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.administration.ArchivalSettingsContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.CaseAccessDefaultsContext;
import se.streamsource.streamflow.web.context.administration.CaseTypeContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.resource.organizations.forms.FormsResource;
import se.streamsource.streamflow.web.resource.organizations.forms.SelectedFormsResource;

/**
 * JAVADOC
 */
public class CaseTypeResource
   extends CommandQueryResource
{
   public CaseTypeResource()
   {
      super( CaseTypeContext.class, DescribableContext.class );
   }

   public void possiblemoveto() throws Throwable
   {
      Iterable<Describable> caseTypes = (Iterable<Describable>) invoke();
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );

      builder.addDescribables( caseTypes );

      result(builder.newLinks());

   }

   @SubResource
   public void forms( )
   {
      subResource( FormsResource.class );
   }

   @SubResource
   public void labels()
   {
      subResource( LabelsResource.class );
   }

   @SubResource
   public void selectedforms()
   {
      subResource( SelectedFormsResource.class );
   }

   @SubResource
   public void selectedlabels()
   {
      subResource( SelectedLabelsResource.class );
   }

   @SubResource
   public void resolutions()
   {
      subResource( ResolutionsResource.class );
   }

   @SubResource
   public void selectedresolutions()
   {
      subResource( SelectedResolutionsResource.class );
   }

   @SubResource
   public void caseaccessdefaults()
   {
      subResourceContexts( CaseAccessDefaultsContext.class );
   }

   @SubResource
   public void archival()
   {
      subResourceContexts(ArchivalSettingsContext.class);
   }
}
