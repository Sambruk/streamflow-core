/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.FormValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.CasePriorityDTO;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresCasePrioritySettingVisibility;
import se.streamsource.streamflow.web.domain.structure.casetype.CasePrioritySetting;
import se.streamsource.streamflow.web.domain.structure.organization.CasePriorityDefinitions;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

/**
 * Context for case priority settings.
 */
@Mixins(CasePrioritySettingContext.Mixin.class)
public interface CasePrioritySettingContext
      extends Context, IndexContext<FormValue>
{

   @RequiresCasePrioritySettingVisibility
   void updatemandatory( @Name("mandatory") Boolean mandatory );

   void updatevisibility( @Name("visible") Boolean visible );

   void defaultpriority( @Optional @Name("name") String name, @Optional @Name("color") String color );

   LinksValue casepriorities();

   abstract class Mixin implements CasePrioritySettingContext, IndexContext<FormValue>
   {
      @Structure
      Module module;

      @Uses
      CasePrioritySetting prioritySetting;

      @Uses
      CasePrioritySetting.Data prioritySettingData;

      public FormValue index()
      {
         ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );
         builder.prototype().form().get().put( "visible", prioritySettingData.visible().get().toString() );
         builder.prototype().form().get().put( "mandatory", prioritySettingData.mandatory().get().toString() );
         builder.prototype().form().get().put( "defaultpriority", prioritySettingData.defaultPriority().get() != null
               ? prioritySettingData.defaultPriority().get().name().get() : "-" );
         return builder.newInstance();
      }

      public void updatevisibility( Boolean visible )
      {
         prioritySetting.changeCasePriorityVisibility( visible );
      }

      public void updatemandatory( Boolean mandatory )
      {
         prioritySetting.changeCasePriorityMandate( mandatory );
      }

      public void defaultpriority( String name, String color )
      {
         if( name != null )
         {
            ValueBuilder<CasePriorityValue> builder = module.valueBuilderFactory().newValueBuilder( CasePriorityValue.class );
            builder.prototype().name().set( name );
            builder.prototype().color().set( color );
            prioritySetting.changeCasePriorityDefault( builder.newInstance() );
         } else
            prioritySetting.changeCasePriorityDefault( null );
      }

      public LinksValue casepriorities()
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Organization org = orgs.organization().get();
         RoleMap.current().set( org );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "defaultpriority" );
         ValueBuilder<CasePriorityDTO> linkBuilder = module.valueBuilderFactory().newValueBuilder( CasePriorityDTO.class );

         linkBuilder.prototype().text().set( "-" );
         linkBuilder.prototype().id().set( "-1" );
         linkBuilder.prototype().href().set( "" );
         builder.addLink( linkBuilder.newInstance() );


         int count = 0;
         for( CasePriorityValue priority : RoleMap.role( CasePriorityDefinitions.Data.class ).prioritys().get() )
         {
            linkBuilder.prototype().priority().set( priority );
            linkBuilder.prototype().text().set( priority.name().get() );
            linkBuilder.prototype().id().set( ""+count );
            linkBuilder.prototype().rel().set( "priority" );
            linkBuilder.prototype().href().set( "" );

            builder.addLink( linkBuilder.newInstance() );
            count++;
         }

         return builder.newLinks();
      }

   }
}
