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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleCondition;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleDefinitionValue;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.VisibilityRule;

import java.util.List;

/**
 * Context for updating visibility rule.
 */
public class VisibilityRuleContext
{
   @Structure
   Module module;

   public VisibilityRuleDefinitionValue rule()
   {
      return RoleMap.role( VisibilityRule.class ).getRule();
   }

   public void changerulefieldid( @Optional @Name("fieldid") String fieldId )
   {
      fieldId = fieldId == null ? "" : fieldId;
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );
      ValueBuilder<VisibilityRuleDefinitionValue> builder = getVisibilityRuleDefinitionValueBuilder( visibilityRule );
      builder.prototype().field().set( fieldId  );

      visibilityRule.changeRule( builder.newInstance() );
   }

   private ValueBuilder<VisibilityRuleDefinitionValue> getVisibilityRuleDefinitionValueBuilder( VisibilityRule visibilityRule )
   {
      ValueBuilder<VisibilityRuleDefinitionValue> builder;
      if( visibilityRule.getRule() == null )
      {
         builder = module.valueBuilderFactory().newValueBuilder( VisibilityRuleDefinitionValue.class );
      } else
      {
         builder = module.valueBuilderFactory().newValueBuilder( VisibilityRuleDefinitionValue.class ).withPrototype( visibilityRule.getRule() );
      }
      return builder;
   }

   public void changerulecondition( @Optional @Name("condition") String condition )
   {
      condition = condition == null ? "anyof" : condition;
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );
      ValueBuilder<VisibilityRuleDefinitionValue> builder = getVisibilityRuleDefinitionValueBuilder( visibilityRule );
      builder.prototype().condition().set( VisibilityRuleCondition.valueOf( condition ) );

      visibilityRule.changeRule( builder.newInstance() );
   }

   public void addrulevalue( @Name("value") String value )
   {
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );
      ValueBuilder<VisibilityRuleDefinitionValue> builder = getVisibilityRuleDefinitionValueBuilder( visibilityRule );
      List<String> values = builder.prototype().values().get();
      values.add( value );
      builder.prototype().values().set( values );

      visibilityRule.changeRule( builder.newInstance() );
   }

   public void removerulevalue( @Name("index") String index )
   {
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );
      ValueBuilder<VisibilityRuleDefinitionValue> builder = getVisibilityRuleDefinitionValueBuilder( visibilityRule );
      List<String> values = builder.prototype().values().get();
      values.remove( Integer.valueOf( index ).intValue() );
      builder.prototype().values().set( values );

      visibilityRule.changeRule( builder.newInstance() );
   }

   public void changerulevaluename( @Name("index") String index, @Name("value") String value )
   {
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );
      ValueBuilder<VisibilityRuleDefinitionValue> builder = getVisibilityRuleDefinitionValueBuilder( visibilityRule );
      List<String> values = builder.prototype().values().get();
      values.set( Integer.valueOf( index ).intValue(), value );
      builder.prototype().values().set( values );

      visibilityRule.changeRule( builder.newInstance() );
   }

   public void changerulevisiblewhen( @Optional @Name("visiblewhen") Boolean visibleWhen )
   {
      visibleWhen = visibleWhen == null ? false : visibleWhen;
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );
      ValueBuilder<VisibilityRuleDefinitionValue> builder = getVisibilityRuleDefinitionValueBuilder( visibilityRule );
      builder.prototype().visibleWhen().set( visibleWhen  );

      visibilityRule.changeRule( builder.newInstance() );
   }

   public LinksValue possibleruleconditions()
   {
      VisibilityRule visibilityRule = RoleMap.role( VisibilityRule.class );

      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
      linksBuilder.command( "changerulecondition" );

      if( visibilityRule.getRule() != null && !Strings.empty( visibilityRule.getRule().field().get() ) )
      {
         FieldValue fieldValue = module.unitOfWorkFactory().currentUnitOfWork().get( FieldValueDefinition.Data.class, visibilityRule.getRule().field().get() ).fieldValue().get();

         linksBuilder.addLink( VisibilityRuleCondition.anyof.name(), VisibilityRuleCondition.anyof.name() );
         linksBuilder.addLink( VisibilityRuleCondition.noneof.name(), VisibilityRuleCondition.noneof.name() );

         if( fieldValue instanceof NumberFieldValue || fieldValue instanceof DateFieldValue )
         {
            linksBuilder.addLink( VisibilityRuleCondition.lessthan.name(), VisibilityRuleCondition.lessthan.name() );
            linksBuilder.addLink( VisibilityRuleCondition.morethan.name(), VisibilityRuleCondition.morethan.name() );
         }
      }
      return linksBuilder.newLinks();
   }
}
