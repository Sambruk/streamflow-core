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

import org.qi4j.api.entity.EntityBuilder;
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
@Mixins(FieldDefinitions.FieldDefinitionsMixin.class)
public interface FieldDefinitions
{
    FieldDefinitionEntity createFieldDefinition(String name, ValueDefinition valueDefinition);
    void removeFieldDefinition(FieldDefinition field);

    interface FieldDefinitionsState
    {
        ManyAssociation<FieldDefinitionEntity> fieldDefinitions();

        FieldDefinitionEntity fieldDefinitionCreated( DomainEvent event, String id, ValueDefinition valueDefinition);
        void fieldDefinitionAdded( DomainEvent event, FieldDefinitionEntity field);
        void fieldDefinitionRemoved( DomainEvent event, FieldDefinition field);

        FieldDefinitionEntity getFieldDefinitionByName(String name);
    }

    abstract class FieldDefinitionsMixin
        implements FieldDefinitions, FieldDefinitionsState
    {
        @Service
        IdentityGenerator idGen;

        @Structure
        UnitOfWorkFactory uowf;

        public FieldDefinitionEntity createFieldDefinition( String name, ValueDefinition valueDefinition )
        {
            String id = idGen.generate( FieldDefinitionEntity.class );

            FieldDefinitionEntity field = fieldDefinitionCreated( DomainEvent.CREATE, id, valueDefinition );
            fieldDefinitionAdded( DomainEvent.CREATE, field );
            field.changeDescription( name );

            return field;
        }

        public FieldDefinitionEntity fieldDefinitionCreated( DomainEvent event, String id, ValueDefinition valueDefinition )
        {
            EntityBuilder<FieldDefinitionEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( FieldDefinitionEntity.class, id );

            builder.instance().valueDefinition().set( valueDefinition );

            return builder.newInstance();
        }

        public FieldDefinitionEntity getFieldDefinitionByName( String name )
        {
            return Describable.DescribableMixin.getDescribable( fieldDefinitions(), name );
        }
    }
}