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

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.constraints.annotation.GreaterThan;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Fields.FieldsMixin.class)
public interface Fields
{
    void addField(FieldDefinition field);
    void removeField(FieldDefinition field);
    void moveField(FieldDefinition field, @GreaterThan(-1) Integer toIdx);

    interface FieldsState
    {
        ManyAssociation<FieldDefinition> fields();

        void fieldAdded( DomainEvent event, FieldDefinition field);
        void fieldRemoved(DomainEvent event, FieldDefinition field);
        void fieldMoved(DomainEvent event, FieldDefinition field, int toIdx);

        FieldDefinitionEntity getFieldByName(String name);
    }

    abstract class FieldsMixin
        implements Fields, FieldsState
    {
        public void addField( FieldDefinition field )
        {
            if (fields().contains( field ))
                return;

            fieldAdded(DomainEvent.CREATE, field);
        }

        public void removeField( FieldDefinition field )
        {
            if (!fields().contains( field ))
                return;

            fieldRemoved( DomainEvent.CREATE, field );
        }

        public void moveField( FieldDefinition field, Integer toIdx )
        {
            if (!fields().contains( field ) || fields().count() < toIdx)
                return;

            fieldMoved( DomainEvent.CREATE, field, toIdx );
        }

        public void fieldMoved( DomainEvent event, FieldDefinition field, int toIdx )
        {
            fields().remove( field );

            fields().add( toIdx, field );
        }

        public FieldDefinitionEntity getFieldByName( String name )
        {
            for (FieldDefinition fieldDefinition : fields())
            {
                if (((Describable.DescribableState)fieldDefinition).description().get().equals(name))
                    return (FieldDefinitionEntity) fieldDefinition;
            }
            return null;
        }
    }
}
