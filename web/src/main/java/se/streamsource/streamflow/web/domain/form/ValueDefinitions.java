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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(ValueDefinitions.Mixin.class)
public interface ValueDefinitions
{
    ValueDefinitionEntity createValueDefinition(String name);
    void removeValueDefinition(ValueDefinition value);

    interface Data
    {
        ManyAssociation<ValueDefinitionEntity> valueDefinitions();

        ValueDefinitionEntity createdValueDefinition( DomainEvent event, String id);
        void removedValueDefinition( DomainEvent event, ValueDefinition value);

        ValueDefinitionEntity getValueDefinitionByName(String name);
    }

    abstract class Mixin
        implements ValueDefinitions, Data
    {
        @Service
        IdentityGenerator idGen;

        @Structure
        UnitOfWorkFactory uowf;

        public ValueDefinitionEntity createValueDefinition( String name )
        {
            ValueDefinitionEntity value = createdValueDefinition( DomainEvent.CREATE, idGen.generate(ValueDefinitionEntity.class ));
            value.changeDescription( name );
            return value;
        }

        public void removeValueDefinition( ValueDefinition value )
        {
            if (valueDefinitions().contains( (ValueDefinitionEntity) value ))
                removedValueDefinition( DomainEvent.CREATE, value );
        }

        public ValueDefinitionEntity createdValueDefinition( DomainEvent event, String id )
        {
            ValueDefinitionEntity value = uowf.currentUnitOfWork().newEntity( ValueDefinitionEntity.class, id );
            valueDefinitions().add( value );
            return value;
        }

        public void removedValueDefinition( DomainEvent event, ValueDefinition value )
        {
            valueDefinitions().remove( (ValueDefinitionEntity) value );
        }

        public ValueDefinitionEntity getValueDefinitionByName( String name )
        {
            return Describable.Mixin.getDescribable( valueDefinitions(), name );
        }
    }
}