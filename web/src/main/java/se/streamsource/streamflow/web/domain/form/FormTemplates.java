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
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(FormTemplates.Mixin.class)
public interface FormTemplates
{
    FormTemplateEntity createFormTemplate(Form fromForm);
    void removeFormTemplate( FormTemplate template);

    interface Data
    {
        ManyAssociation<FormTemplateEntity> formTemplates();
        
        FormTemplateEntity createdFormTemplate( DomainEvent event, String id);
        void removedFormTemplate( DomainEvent event, FormTemplate template);

        FormTemplateEntity getTemplateByName(String name);
    }

    abstract class Mixin
        implements FormTemplates, Data
    {
        @Service
        IdentityGenerator idGen;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        QueryBuilderFactory qbf;

        public FormTemplateEntity createFormTemplate( Form fromForm )
        {
            FormTemplateEntity template = createdFormTemplate( DomainEvent.CREATE, idGen.generate( FormTemplateEntity.class ));

            FormEntity form = (FormEntity) fromForm;

            template.changeDescription( form.description().get() );
            template.changeNote( form.note().get() );

            for (Field field : form.fields())
            {
                FieldEntity templateField = template.createField( field.getDescription(), ((FieldValueDefinition.Data)field).fieldValue().get() );
                templateField.copyFromTemplate( field );
            }

            return form;
        }

        public void removeFormTemplate( FormTemplate template )
        {
            QueryBuilder<FormTemplateReference> qb = qbf.newQueryBuilder( FormTemplateReference.class );
            FormTemplateReference.Data ftr = QueryExpressions.templateFor( FormTemplateReference.Data.class );
            Query<FormTemplateReference> query = qb.where( QueryExpressions.eq( ftr.template(), template) ).newQuery( uowf.currentUnitOfWork() );
            for (FormTemplateReference formTemplateReference : query)
            {
                ((FormTemplateReference.Data)formTemplateReference).removedTemplateReference(DomainEvent.CREATE);
            }

            removedFormTemplate(DomainEvent.CREATE, template);
        }

        public void removedFormTemplate( DomainEvent event, FormTemplate template )
        {
            formTemplates().remove( (FormTemplateEntity) template );
            uowf.currentUnitOfWork().remove( template );
        }

        public FormTemplateEntity getTemplateByName( String name )
        {
            return Describable.Mixin.getDescribable( formTemplates(), name );
        }
    }
}
