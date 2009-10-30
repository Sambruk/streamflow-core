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
@Mixins(FieldTemplates.Mixin.class)
public interface FieldTemplates
{
    FieldEntity createFieldTemplate(String name, ValueDefinition valueDefinition);
    void removeFieldDefinition( Field field);

    interface Data
    {
        ManyAssociation<FieldEntity> fieldDefinitions();

        FieldEntity createdFieldDefinition( DomainEvent event, String id, ValueDefinition valueDefinition);
        void addedFieldDefinition( DomainEvent event, FieldEntity field);
        void removedFieldDefinition( DomainEvent event, Field field);

        FieldEntity getFieldDefinitionByName(String name);
    }

    abstract class Mixin
        implements FieldTemplates, Data
    {
        @Service
        IdentityGenerator idGen;

        @Structure
        UnitOfWorkFactory uowf;

        public FieldEntity createFieldTemplate( String name, ValueDefinition valueDefinition )
        {
            String id = idGen.generate( FieldEntity.class );

            FieldEntity field = createdFieldDefinition( DomainEvent.CREATE, id, valueDefinition );
            addedFieldDefinition( DomainEvent.CREATE, field );
            field.changeDescription( name );

            return field;
        }

        public FieldEntity createdFieldDefinition( DomainEvent event, String id, ValueDefinition valueDefinition )
        {
            EntityBuilder<FieldEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( FieldEntity.class, id );

            builder.instance().valueDefinition().set( valueDefinition );

            return builder.newInstance();
        }

        public FieldEntity getFieldDefinitionByName( String name )
        {
            return Describable.Mixin.getDescribable( fieldDefinitions(), name );
        }
    }
}