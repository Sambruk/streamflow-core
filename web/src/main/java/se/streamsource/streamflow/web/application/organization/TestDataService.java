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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.form.Field;
import se.streamsource.streamflow.web.domain.form.Form;
import se.streamsource.streamflow.web.domain.form.FormTemplates;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectRole;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.util.Date;
import java.util.List;

/**
 * Generates test data
 */
@Mixins(TestDataService.Mixin.class)
public interface TestDataService
        extends ServiceComposite, Activatable
{
    class Mixin
            implements Activatable
    {
        @Structure
        Application app;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public void activate() throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Test data"));

            UserEntity user = uow.get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);

            OrganizationEntity ou = (OrganizationEntity) user.organizations().iterator().next();
            ou.changeDescription("WayGroup");

            // Create suborganizations
            OrganizationalUnitRefactoring jayway = ou.createOrganizationalUnit("Jayway");
            ou.createOrganizationalUnit("Dotway");
            ou.createOrganizationalUnit("Realway");

            // Create groups
            Group cc = ou.createGroup("Contact center");
            ou.createGroup("Park management");

            cc.addParticipant(user);

            FormTemplates forms = (FormTemplates) ou;

            ProjectRole agent = ou.createProjectRole("Agent");
            ProjectRole manager = ou.createProjectRole("Manager");

            // Create tasks
            for (int i = 0; i < 30; i++)
                user.createTask().changeDescription("Arbetsuppgift " + i);

            // Create project
            Project project = ou.createProject("Information query");

            Form commentForm = project.createForm();
            commentForm.changeDescription( "CommentForm" );
            commentForm.changeNote("This is a comment form. Use it to capture any comments related to the current task.");
            ValueBuilder<FieldValue> builder = vbf.newValueBuilder(FieldValue.class);
            Field commentField = commentForm.createField( "Comment", builder.newInstance());

            Form statusForm = project.createForm();
            statusForm.changeDescription("StatusForm");
            statusForm.changeNote("This is the Status form. \nWhen urgencies occur please upgrade the status of the current task");
            Field statusField = statusForm.createField( "Status", builder.newInstance() );

            ou.createFormTemplate( commentForm );

            Form addressForm = project.createForm();
            addressForm.changeDescription( "Address form" );
            addressForm.changeNote("Address form of the task");
            addressForm.createField( "Street", builder.newInstance() ).changeNote("Street of the address. Note that it must only be the the street name of the address not the number");
            addressForm.createField( "Zip code", builder.newInstance() ).changeNote("This is the ZIP code of the resident");
            addressForm.createField( "Town", builder.newInstance() ).changeNote("Town of the address.");

            // Create labels
            project.createLabel().changeDescription("Question");
            project.createLabel().changeDescription("Issue chase");
            project.createLabel().changeDescription("Suggestion");

            for (int i = 0; i < 50; i++)
                project.createLabel().changeDescription("Label "+i);

            project.addMember(user);

            // Create project
            Project info2 = ou.createProject("Info query");

            info2.addMember(cc);
            info2.addMember(user);

            Project parks = ou.createProject("City parks");

            parks.addMember(cc);
            parks.addMember(user);

            // Create tasks
            Task task = project.createTask();
            task.changeDescription("Arbetsuppgift 0");

            SubmittedFormValue submitted = createSubmittedForm(user, commentForm, commentField, "Remember that this Task is important" );
            task.submitForm(submitted);

            submitted = createSubmittedForm(user, statusForm, statusField, "Progress is slow");
            task.submitForm(submitted);

            submitted = createSubmittedForm(user, statusForm, statusField, "Progress is getting better");
            task.submitForm(submitted);



            for (int i = 1; i < 30; i++)
                project.createTask().changeDescription("Arbetsuppgift " + i);

            // Create labels
            for (int i = 1; i < 10; i++)
            user.createLabel().changeDescription("Label " + i);


            uow.complete();
        }

        private SubmittedFormValue createSubmittedForm(UserEntity user, Form form, Field field, String value)
        {
            ValueBuilder<SubmittedFormValue> builder = vbf.newValueBuilder(SubmittedFormValue.class);
            builder.prototype().submissionDate().set(new Date());
            builder.prototype().submitter().set(EntityReference.getEntityReference(user));
            builder.prototype().form().set(EntityReference.getEntityReference(form));

            List<SubmittedFieldValue> list = builder.prototype().values().get();

            ValueBuilder<SubmittedFieldValue> fieldBuilder = vbf.newValueBuilder(SubmittedFieldValue.class);
            fieldBuilder.prototype().field().set(EntityReference.getEntityReference(field));
            fieldBuilder.prototype().value().set(value);
            list.add(fieldBuilder.newInstance());

            return builder.newInstance();
        }


        public void passivate() throws Exception
        {
        }
    }
}