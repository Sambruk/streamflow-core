/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource.surface.accesspoints.endusers.submittedforms;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SurfaceSubmittedFormsContext;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;

/**
 * JAVADOC
 */
public class SurfaceSubmittedFormsResource
      extends CommandQueryResource
      implements SubResources
{
   public SurfaceSubmittedFormsResource()
   {
      super( SurfaceSubmittedFormsContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      SubmittedForms.Data data = RoleMap.role( SubmittedForms.Data.class );

      for (SubmittedFormValue value : data.submittedForms().get())
      {

         if (value.form().get().identity().equals( segment ))
         {

            RoleMap.current().set( value );
            subResource(SurfaceSubmittedFormResource.class);
            return;
         }
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }
}