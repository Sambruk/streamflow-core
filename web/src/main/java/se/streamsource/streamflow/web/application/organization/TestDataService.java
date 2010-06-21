/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.Page;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRole;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.Users;

import javax.security.auth.Subject;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

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
         Subject subject = new Subject();
         subject.getPrincipals().add( new UserPrincipal("administrator") );
         Subject.doAs( subject, new PrivilegedExceptionAction()
         {
            public Object run() throws Exception
            {
               UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Test data" ) );

               UserEntity user = uow.get( UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME );

               Organizations orgs = uow.get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );
               Users users = uow.get( Users.class, UsersEntity.USERS_ID );

               User testUser = users.createUser( "testuser", "testuser" );
               User someUser = users.createUser( "someuser", "someuser" );
               User someUser2 = users.createUser( "someuser2", "someuser2" );

               // contact info on someuser
               ValueBuilder<ContactValue> contact = vbf.newValueBuilder( ContactValue.class );
               ValueBuilder<ContactEmailValue> email = vbf.newValueBuilder( ContactEmailValue.class );
               email.prototype().emailAddress().set( "streamsourceflow@gmail.com" );
               email.prototype().contactType().set( ContactEmailValue.ContactType.WORK );
               List<ContactEmailValue> list = new ArrayList<ContactEmailValue>();
               list.add(email.newInstance());
               contact.prototype().emailAddresses().set( list );

               ((Contactable)someUser).updateContact( contact.newInstance() );


               OrganizationEntity organization = (OrganizationEntity) user.organizations().iterator().next();
               organization.changeDescription( "WayGroup" );
               testUser.join( organization );
               someUser.join( organization );
               someUser2.join( organization );

               Label question = organization.createLabel( "Question" );
               Label issueChase = organization.createLabel( "Issue chase" );
               Label suggestion = organization.createLabel( "Suggestion" );

               Label minor = organization.createLabel( "Minor" );
               Label major = organization.createLabel( "Major" );
               Label critical = organization.createLabel( "Critical" );

               // Create case types
               CaseType newFeature = organization.createCaseType( "New feature" );
               CaseType bug = organization.createCaseType( "Bug" );
               bug.addSelectedLabel( minor );
               bug.addSelectedLabel( major );
               bug.addSelectedLabel( critical );
               Resolution fixed = bug.createResolution( "Fixed" );
               Resolution wontFix = bug.createResolution( "Won't fix" );
               bug.addSelectedResolution( fixed );
               bug.addSelectedResolution( wontFix );
               CaseType improvement = organization.createCaseType( "Improvement" );
               improvement.addSelectedLabel( minor );
               improvement.addSelectedLabel( major );
               Resolution implemented = improvement.createResolution( "Implemented" );
               Resolution rejected = improvement.createResolution( "Rejected" );
               improvement.addSelectedResolution( implemented );
               improvement.addSelectedResolution( rejected );
               CaseType complaint = organization.createCaseType( "Complaint" );
               CaseType passwordReset = organization.createCaseType( "Reset password" );

               // Create suborganizations
               OrganizationalUnit jayway = organization.createOrganizationalUnit( "Jayway" );
               OrganizationalUnit streamSource = organization.createOrganizationalUnit( "StreamSource" );
               OrganizationalUnit admin = organization.createOrganizationalUnit( "Administration" );

               // Create groups
               Group developers = jayway.createGroup( "Developers" );
               Group admins = admin.createGroup( "Administrators" );

               developers.addParticipant( user );
               developers.addParticipant( someUser );

               admins.addParticipant( testUser );
               admins.addParticipant( someUser );

               ProjectRole agent = organization.createProjectRole( "Agent" );
               ProjectRole manager = organization.createProjectRole( "Manager" );

               // Create draft cases
               for (int i = 0; i < 30; i++)
               {
                  CaseEntity aCase = user.createDraft();
                  aCase.changeDescription( "Ärende " + i );

                  if (i>20)
                  {
                     aCase.assignTo( user );

                     Conversation conversation = aCase.createConversation( "Questions " + i , testUser );
                     ConversationEntity conversationEntity = (ConversationEntity) conversation;
                     conversationEntity.addParticipant( testUser );
                     conversationEntity.addParticipant( someUser );
                     conversationEntity.addParticipant( someUser2 );
                     conversation.createMessage( "Test message " + i, testUser );
                     conversation.createMessage( "In reply " + i , someUser );
                  }
               }


               // Create project
               Project project = jayway.createProject( "Streamflow" );

               project.addSelectedCaseType( newFeature );
               project.addSelectedCaseType( bug );
               project.addSelectedCaseType( improvement );

               Form bugreport = bug.createForm();
               bugreport.changeDescription( "Bug Report" );
               bugreport.changeNote( "A form to capture a bug report" );
               ValueBuilder<TextFieldValue> builder = vbf.newValueBuilder( TextFieldValue.class );
               builder.prototype().width().set( 30 );
               ValueBuilder<DateFieldValue> dateBuilder = vbf.newValueBuilder( DateFieldValue.class );
               ValueBuilder<NumberFieldValue> numberBuilder = vbf.newValueBuilder( NumberFieldValue.class );
               ValueBuilder<SelectionFieldValue> selectionBuilder = vbf.newValueBuilder( SelectionFieldValue.class );
               List<String> values = new ArrayList<String>();
               values.add( "Critical" );
               values.add( "High" );
               values.add( "Normal" );
               values.add( "Low" );
               selectionBuilder.prototype().values().set( values );
               Page page = bugreport.createPage( "General Info" );
               page.createField( "Bugname", builder.newInstance() ).changeMandatory( true );
               numberBuilder.prototype().integer().set( true );
               page.createField( "Bug ID", numberBuilder.newInstance() ).changeMandatory( true );
               builder.prototype().rows().set( 5 );
               page.createField( "Description", builder.newInstance() );
               page = bugreport.createPage( "Date Information" );
               page.createField( "Discovered", dateBuilder.newInstance() ).changeMandatory( true );
               page.createField( "Priority", selectionBuilder.newInstance() ).changeMandatory( true );
               selectionBuilder.prototype().multiple().set( true );
               values.clear();
               values.add( "Server" );
               values.add( "Client" );
               page.createField( "Bug Location", selectionBuilder.newInstance() ).changeNote( "Indicate what part of the application the bug is. Optional" );
               bug.addSelectedForm( bugreport );

               Form statusForm = bug.createForm();
               statusForm.changeDescription( "StatusForm" );
               statusForm.changeNote( "This is the Status form. \nWhen urgencies occur please upgrade the status of the current aCase" );
               page = statusForm.createPage( "Status Form" );
               page.createField( "Status", builder.newInstance() ).changeMandatory( true );
               bug.addSelectedForm( statusForm );

               Form emailForm = improvement.createForm();
               emailForm.changeDescription( "Email form" );
               emailForm.changeNote( "Form for entering and sending an email" );
               builder.prototype().rows().set( 0 );
               page = emailForm.createPage( "Email Form" );
               page.createField( "To", builder.newInstance() ).changeNote( "Enter address of receiver. Note it must be a valid email" );
               page.createField( "Subject", builder.newInstance() ).changeNote( "Subject of the mail" );
               builder.prototype().rows().set( 10 );
               page.createField( "Content", builder.newInstance() ).changeNote( "Mail content" );
               improvement.addSelectedForm( emailForm );

               Form resetPasswordForm = passwordReset.createForm();
               resetPasswordForm.changeDescription( "Reset password" );
               resetPasswordForm.changeNote( "Reset password for a user" );
               builder.prototype().rows().set( 0 );
               page = resetPasswordForm.createPage( "Reset password form" );
               page.createField( "Username", builder.newInstance() ).changeNote( "Username whose password should be reset" );
               passwordReset.addSelectedForm( resetPasswordForm );

               Form complaintForm = complaint.createForm();
               complaintForm.changeDescription( "Complaint" );
               complaintForm.changeNote( "This form is to file in a complaint" );
               page = complaintForm.createPage( "Complaint" );
               page.createField( "Name", builder.newInstance() );
               page.createField( "Email", builder.newInstance() );
               page.createField( "Complaint", builder.newInstance() );
               complaint.addSelectedForm( complaintForm );

               // Create labels
               project.addSelectedLabel( question );
               project.addSelectedLabel( issueChase );
               project.addSelectedLabel( suggestion );

               for (int i = 0; i < 50; i++)
               {
                  Label label = organization.createLabel( "Label " + i );
                  project.addSelectedLabel( label );
               }

               project.addMember( user );

               // Create project
               Project info2 = jayway.createProject( "StreamForm" );
               info2.addSelectedCaseType( newFeature );
               info2.addSelectedCaseType( bug );
               info2.addSelectedCaseType( improvement );

               info2.addMember( (Member) developers );
               info2.addMember( user );

               Project itSupport = admin.createProject( "IT support" );
               itSupport.addSelectedCaseType( passwordReset );
               itSupport.addMember( user );

               Project invoicing = admin.createProject( "Invoicing" );
               invoicing.addSelectedCaseType( complaint );
               invoicing.addMember( user );

               // Create cases
               CaseEntity aCase = user.createDraft();
               aCase.changeDescription( "Ärende 0" );
               aCase.sendTo( (Owner) project );

               aCase.changeCaseType( bug );
               aCase.open();

               FormSubmission formSubmission = aCase.createFormSubmission( statusForm );
               submitStatus( aCase, formSubmission, "Progress is slow", (Submitter) testUser );
               submitStatus( aCase, formSubmission, "Progress is getting better", (Submitter) someUser );

               for (int i = 1; i < 30; i++)
               {
                  CaseEntity caze = user.createDraft();
                  caze.changeDescription( "Ärende " + i );
                  caze.sendTo( (Owner) project );
                  caze.open();
               }

               // Access test data
               ArrayList<Label> labels = new ArrayList<Label>();
               labels.add( question );
               organization.createAccessPoint( "ComplaintAccess", invoicing, complaint, labels );

               //ProxyUser proxyUser = organization.createProxyUser( "Citizens Portal", "proxy", "proxy" );

               uow.complete();

               return null;
            }
         });

      }

      private void submitStatus( Case aCase, FormSubmission formSubmission, String status, Submitter submitter )
      {
         FormSubmissionValue submissionValue = (FormSubmissionValue) formSubmission.getFormSubmission().buildWith().prototype();
         for (PageSubmissionValue pageValue : submissionValue.pages().get())
         {
            for (FieldSubmissionValue value : pageValue.fields().get())
            {
               value.value().set( status );
            }
         }
         formSubmission.changeFormSubmission( submissionValue );
         aCase.submitForm( formSubmission, submitter );
      }


      public void passivate() throws Exception
      {
      }

   }
}