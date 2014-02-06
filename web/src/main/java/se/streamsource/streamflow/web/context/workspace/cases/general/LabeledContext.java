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
package se.streamsource.streamflow.web.context.workspace.cases.general;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;

/**
 * JAVADOC
 */
public class LabeledContext
      implements DeleteContext
{
   @Structure
   Module module;

   @Service
   KnowledgebaseService knowledgebaseService;

   public void delete()
   {
      role( Labelable.class ).removeLabel( role( Label.class ) );
   }


   @ServiceAvailable( service = KnowledgebaseService.class, availability = true)
   public LinkValue knowledgeBase()
   {
      LabelEntity label = RoleMap.role(LabelEntity.class);
      ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
      builder.prototype().id().set(label.identity().get());
      builder.prototype().text().set(label.getDescription());
      builder.prototype().rel().set("knowledgebase");
      builder.prototype().href().set(knowledgebaseService.createURL(label));
      return builder.newInstance();
   }
}
