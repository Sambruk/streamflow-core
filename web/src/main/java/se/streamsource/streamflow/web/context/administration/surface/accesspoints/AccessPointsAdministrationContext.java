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

package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.organization.*;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Mixins(AccessPointsAdministrationContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface AccessPointsAdministrationContext
      extends IndexContext<LinksValue>, Context
{
   public void createaccesspoint( @MaxLength(50) StringValue name );

   abstract class Mixin
         implements AccessPointsAdministrationContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         AccessPoints.Data data = role( AccessPoints.Data.class );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( data.accessPoints() );

         return linksBuilder.newLinks();
      }

      public void createaccesspoint( StringValue name )
      {
         AccessPoints accessPoints = role( AccessPoints.class );
         accessPoints.createAccessPoint( name.string().get() );
      }
   }
}