/**
 *
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

package se.streamsource.streamflow.web.context.surface.administration.organizations.accesspoints;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;

/**
 * JAVADOC
 */
@Mixins(AccessPointContext.Mixin.class)
public interface AccessPointContext
   extends IndexInteraction<StringValue>, Interactions, DeleteInteraction
{
   abstract class Mixin
      extends InteractionsMixin
      implements AccessPointContext
   {
      public StringValue index()
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         StringBuilder sb = new StringBuilder( );
         AccessPoint accessPoint = context.get( AccessPoint.class );
         AccessPoint.Data data = context.get( AccessPoint.Data.class );
         sb.append( accessPoint.getDescription() ).append( "(" ).append( EntityReference.getEntityReference( accessPoint ) ).append( ")" );
         sb.append( ": Project="  );
         sb.append( data.project().get().getDescription() );
         sb.append( ", Case Type=" );
         sb.append( data.caseType().get().getDescription() );
         sb.append( ", Label(s)=" );

         for (Label label : data.labels())
         {
            sb.append( label.getDescription() );
            sb.append( " " );
         }

         builder.prototype().string().set( sb.toString() );
         return builder.newInstance();
      }

      public void delete() throws ResourceException
      {
         AccessPoint accessPoint = context.get( AccessPoint.class );
         AccessPoints accessPoints = context.get( AccessPoints.class );

         accessPoints.removeAccessPoint( accessPoint );
      }
   }
}