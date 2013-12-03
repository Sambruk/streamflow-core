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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleDefinitionValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;

/**
 * This interface holds information about visibility rules regarding form fields
 * May be null.
 */
@Mixins(VisibilityRule.Mixin.class)
public interface VisibilityRule
{
   VisibilityRuleDefinitionValue getRule();

   void changeRule( VisibilityRuleDefinitionValue rule );

   /**
    * Validate the rule.
    * @param value The value to test the rule against - might be null for invisible fields
    * @return A boolean whether the rule is valid or not.
    */
   boolean validate ( @Optional String value );

   interface Data
   {
      @Optional
      Property<VisibilityRuleDefinitionValue> rule();

      void changedRule( @Optional DomainEvent event, VisibilityRuleDefinitionValue rule );
   }

   class Mixin
      implements VisibilityRule
   {
      @Structure
      Module module;

      @This
      Data data;

      public VisibilityRuleDefinitionValue getRule()
      {
         if( data.rule().get() == null )
         {
            ValueBuilder<VisibilityRuleDefinitionValue> builder = module.valueBuilderFactory().newValueBuilder( VisibilityRuleDefinitionValue.class );
            builder.prototype().visibleWhen().set( true );
            changeRule( builder.newInstance() );
         }
         return data.rule().get();
      }

      public void changeRule( VisibilityRuleDefinitionValue rule )
      {
         data.changedRule( null, rule );
      }

      public boolean validate( String value )
      {
         boolean result = false;

         VisibilityRuleDefinitionValue rule = getRule();

         // rule not defined return true
         if( rule == null || Strings.empty( rule.field().get() ) )
            return true;

         switch( rule.condition().get() )
         {
            case anyof:
               result = rule.values().get().contains( value );
               break;

            case noneof:
               result = !rule.values().get().contains( value );
               break;

            case lessthan:
               break;

            case morethan:
               break;
         }

         if( rule.visibleWhen().get() )
         {
            return result ? true : false;
         } else
         {
            return result ? false : true;
         }

      }
   }
}
