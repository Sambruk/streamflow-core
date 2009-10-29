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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.domain.form.EffectiveFieldValue;
import se.streamsource.streamflow.domain.form.EffectiveFormFieldsValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;


import java.util.LinkedHashMap;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(SubmittedForms.Mixin.class)
public interface SubmittedForms
{
    void submitForm(SubmittedFormValue form);

    interface Data
    {
        @UseDefaults
        Property<List<SubmittedFormValue>> submittedForms();

        @Optional
        Property<EffectiveFormFieldsValue> effectiveFieldValues();

        void formSubmitted( DomainEvent event, SubmittedFormValue form);

        String getEffectiveValue(FieldDefinition field);
    }

    abstract class Mixin
        implements SubmittedForms, Data
    {
        @Structure
        ValueBuilderFactory vbf;

        public void submitForm( SubmittedFormValue form )
        {
            formSubmitted( DomainEvent.CREATE, form);
        }

        public void formSubmitted( DomainEvent event, SubmittedFormValue form )
        {
            submittedForms().get().add( form );

            //Recalculate effective values
            ValueBuilder<EffectiveFieldValue> fieldBuilder = vbf.newValueBuilder( EffectiveFieldValue.class );

            LinkedHashMap<EntityReference, EffectiveFieldValue> effectiveValues = new LinkedHashMap<EntityReference, EffectiveFieldValue>( );
            for (SubmittedFormValue submittedFormValue : submittedForms().get())
            {
                fieldBuilder.prototype().submissionDate().set( submittedFormValue.submissionDate().get() );
                fieldBuilder.prototype().submitter().set( submittedFormValue.submitter().get() );

                for (FieldValue fieldValue : submittedFormValue.values().get())
                {
                    fieldBuilder.prototype().field().set( fieldValue.field().get() );
                    fieldBuilder.prototype().value().set( fieldValue.value().get() );
                    effectiveValues.put( fieldValue.field().get(), fieldBuilder.newInstance() );
                }
            }

            ValueBuilder<EffectiveFormFieldsValue> fieldsBuilder = vbf.newValueBuilder( EffectiveFormFieldsValue.class );
            List<EffectiveFieldValue> effectiveFieldValues = fieldsBuilder.prototype().fields().get();
            effectiveFieldValues.addAll( effectiveValues.values() );

            EffectiveFormFieldsValue effectiveFormFieldsValue = fieldsBuilder.newInstance();

            effectiveFieldValues().set( effectiveFormFieldsValue );
        }

        public String getEffectiveValue( FieldDefinition field )
        {
            EffectiveFormFieldsValue effectiveFormFieldsValue = effectiveFieldValues().get();
            if (effectiveFormFieldsValue == null)
                return null;

            // Find value among effective fields collection
            EntityReference fieldRef = EntityReference.getEntityReference( field );
            for (EffectiveFieldValue effectiveFieldValue : effectiveFieldValues().get().fields().get())
            {
                if (effectiveFieldValue.field().get().equals(fieldRef))
                    return effectiveFieldValue.value().get();
            }

            // No such field has been submitted
            return null;
        }
    }
}
