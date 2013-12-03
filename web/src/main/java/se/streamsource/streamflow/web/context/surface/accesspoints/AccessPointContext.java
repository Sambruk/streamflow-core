/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.context.surface.accesspoints;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;

/**
 * JAVADOC
 */
public class AccessPointContext
      implements IndexContext<StringValue>
{
   @Structure
   Module module;

   public StringValue index()
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
      StringBuilder sb = new StringBuilder();
      AccessPoint accessPoint = RoleMap.role( AccessPoint.class );
      AccessPointSettings.Data data = RoleMap.role( AccessPointSettings.Data.class );
      Labelable.Data labelsData = RoleMap.role( Labelable.Data.class );
      sb.append( accessPoint.getDescription() ).append( "(" ).append( EntityReference.getEntityReference( accessPoint ) ).append( ")" );
      sb.append( ": Project=" );
      sb.append( data.project().get().getDescription() );
      sb.append( ", Case Type=" );
      sb.append( data.caseType().get().getDescription() );
      sb.append( ", Label(s)=" );

      for (Label label : labelsData.labels())
      {
         sb.append( label.getDescription() );
         sb.append( " " );
      }

      builder.prototype().string().set( sb.toString() );
      return builder.newInstance();
   }
}