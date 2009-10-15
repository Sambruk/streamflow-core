/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.form;

import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(ValueDefinitions.ValueDefinitionsMixin.class)
public interface ValueDefinitions
{
    ValueDefinitionEntity createValueDefinition(String name);
    void removeValueDefinition(ValueDefinition value);

    interface ValueDefinitionsState
    {
        ManyAssociation<ValueDefinitionEntity> valueDefinitions();

        ValueDefinitionEntity valueDefinitionCreated( DomainEvent event, String id);
        void valueDefinitionAdded( DomainEvent event, ValueDefinitionEntity value);
        void valueDefinitionRemoved( DomainEvent event, ValueDefinition value);

        ValueDefinitionEntity getValueDefinitionByName(String name);
    }

    abstract class ValueDefinitionsMixin
        implements ValueDefinitions, ValueDefinitionsState
    {
        @Service
        IdentityGenerator idGen;

        public ValueDefinitionEntity createValueDefinition( String name )
        {
            ValueDefinitionEntity value = valueDefinitionCreated( DomainEvent.CREATE, idGen.generate(ValueDefinitionEntity.class ));
            valueDefinitionAdded( DomainEvent.CREATE, value);
            value.changeDescription( name );
            return value;
        }

        public ValueDefinitionEntity getValueDefinitionByName( String name )
        {
            for (ValueDefinitionEntity valueDefinition : valueDefinitions())
            {
                if (valueDefinition.description().get().equals(name))
                    return valueDefinition;
            }

            return null;
        }
    }
}