/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleDefinitionValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * This interface holds information about visibility rules regarding form fields
 * May be null.
 */
@Mixins(VisibilityRule.Mixin.class)
public interface VisibilityRule
{
   VisibilityRuleDefinitionValue getRule();

   void changeRule( VisibilityRuleDefinitionValue rule );

   interface Data
   {
      @Optional
      Property<VisibilityRuleDefinitionValue> rule();

      void changedRule( @Optional DomainEvent event, VisibilityRuleDefinitionValue rule );
   }

   class Mixin
      implements VisibilityRule
   {
      @This
      Data data;

      public VisibilityRuleDefinitionValue getRule()
      {
         return data.rule().get();
      }

      public void changeRule( VisibilityRuleDefinitionValue rule )
      {
         data.changedRule( null, rule );
      }
   }
}
