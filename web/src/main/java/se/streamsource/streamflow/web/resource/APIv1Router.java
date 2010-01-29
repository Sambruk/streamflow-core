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

package se.streamsource.streamflow.web.resource;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.rest.ExtensionMediaTypeFilter;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.query.IndexResource;
import org.qi4j.rest.query.SPARQLResource;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;
import org.restlet.security.ChallengeAuthenticator;
import se.streamsource.streamflow.web.resource.admin.ConsoleServerResource;
import se.streamsource.streamflow.web.resource.events.EventsServerResource;
import se.streamsource.streamflow.web.resource.labels.LabelServerResource;
import se.streamsource.streamflow.web.resource.labels.LabelsServerResource;
import se.streamsource.streamflow.web.resource.labels.SelectedLabelServerResource;
import se.streamsource.streamflow.web.resource.labels.SelectedLabelsServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationalUnitServerResource;
import se.streamsource.streamflow.web.resource.organizations.OrganizationsServerResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormTemplateServerResource;
import se.streamsource.streamflow.web.resource.organizations.forms.FormTemplatesServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.GroupsServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantServerResource;
import se.streamsource.streamflow.web.resource.organizations.groups.participants.ParticipantsServerResource;
import se.streamsource.streamflow.web.resource.organizations.organizationalunits.OrganizationalUnitsServerResource;
import se.streamsource.streamflow.web.resource.organizations.policy.AdministratorServerResource;
import se.streamsource.streamflow.web.resource.organizations.policy.AdministratorsServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.ProjectServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MemberServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.members.MembersServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.tasktypes.SelectedTaskTypeServerResource;
import se.streamsource.streamflow.web.resource.organizations.projects.tasktypes.SelectedTaskTypesServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RoleServerResource;
import se.streamsource.streamflow.web.resource.organizations.roles.RolesServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.TaskTypeServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.TaskTypesServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.FormDefinitionServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.FormDefinitionsServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages.fields.FormDefinitionFieldsServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages.fields.FormDefinitionFieldServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages.FormDefinitionPagesServerResource;
import se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages.FormDefinitionPageServerResource;
import se.streamsource.streamflow.web.resource.task.TaskActionsServerResource;
import se.streamsource.streamflow.web.resource.task.TaskServerResource;
import se.streamsource.streamflow.web.resource.task.comments.TaskCommentsServerResource;
import se.streamsource.streamflow.web.resource.task.contacts.TaskContactServerResource;
import se.streamsource.streamflow.web.resource.task.contacts.TaskContactsServerResource;
import se.streamsource.streamflow.web.resource.task.forms.TaskFormServerResource;
import se.streamsource.streamflow.web.resource.task.forms.TaskFormsServerResource;
import se.streamsource.streamflow.web.resource.task.general.TaskGeneralServerResource;
import se.streamsource.streamflow.web.resource.users.UserAccessFilter;
import se.streamsource.streamflow.web.resource.users.UsersRouter;
import se.streamsource.streamflow.web.resource.users.UsersServerResource;
import se.streamsource.streamflow.web.rest.CompositeFinder;
import se.streamsource.streamflow.web.rest.ResourceFinder;

/**
 * Router for v1 of the StreamFlow REST API.
 */
public class APIv1Router
      extends Router
{
   private ObjectBuilderFactory factory;

   public APIv1Router( @Uses Context context, @Structure ObjectBuilderFactory factory )
   {
      super( context );
      this.factory = factory;

      attach( createServerResourceFinder( StreamFlowServerResource.class ) );

      // Users
      attach( "/users", createServerResourceFinder( UsersServerResource.class, false ) );

      UserAccessFilter userFilter = factory.newObject( UserAccessFilter.class );
      userFilter.setNext( factory.newObjectBuilder( UsersRouter.class ).use( context ).newInstance() );
      attach( "/users/{user}", userFilter );

      attach( "/users/{labels}/workspace/user/labels", createServerResourceFinder( LabelsServerResource.class ) );
      attach( "/users/{labels}/workspace/user/labels/{label}", createServerResourceFinder( LabelServerResource.class ) );

//      attach ("/users", createServerResourceFinder( CompositeCommandQueryServerResource.class ));

      // Organizations
      attach( "/organizations", createServerResourceFinder( OrganizationsServerResource.class ) );
// Test of composite CQSR        attach("/organizations/{entity}", createServerCompositeFinder(OrganizationCompositeResource.class));
      attach( "/organizations/{organization}", createServerResourceFinder( OrganizationServerResource.class ) );
      attach( "/organizations/{organization}/roles", createServerResourceFinder( RolesServerResource.class ) );
      attach( "/organizations/{organization}/roles/{role}", createServerResourceFinder( RoleServerResource.class ) );
      attach( "/organizations/{organization}/forms", createServerResourceFinder( FormTemplatesServerResource.class ) );
      attach( "/organizations/{organization}/forms/{form}", createServerResourceFinder( FormTemplateServerResource.class ) );
      attach( "/organizations/{labels}/selectedlabels", createServerResourceFinder( SelectedLabelsServerResource.class ) );
      attach( "/organizations/{labels}/selectedlabels/{label}", createServerResourceFinder( SelectedLabelServerResource.class ) );
      attach( "/organizations/{policy}/administrators", createServerResourceFinder( AdministratorsServerResource.class ) );
      attach( "/organizations/{organization}/administrators/{administrator}", createServerResourceFinder( AdministratorServerResource.class ) );
      attach( "/organizations/{organizationalunits}/organizationalunits", createServerResourceFinder( OrganizationalUnitsServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes", createServerResourceFinder( TaskTypesServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{tasktype}", createServerResourceFinder( TaskTypeServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{labels}/selectedlabels", createServerResourceFinder( SelectedLabelsServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{labels}/selectedlabels/{label}", createServerResourceFinder( SelectedLabelServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{forms}/forms", createServerResourceFinder( FormDefinitionsServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{forms}/forms/{form}", createServerResourceFinder( FormDefinitionServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{forms}/forms/{form}/pages", createServerResourceFinder( FormDefinitionPagesServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{forms}/forms/{form}/pages/{page}", createServerResourceFinder( FormDefinitionPageServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{forms}/forms/{form}/pages/{page}/fields", createServerResourceFinder( FormDefinitionFieldsServerResource.class ) );
      attach( "/organizations/{organization}/tasktypes/{forms}/forms/{form}/pages/{page}/fields/{index}", createServerResourceFinder( FormDefinitionFieldServerResource.class ) );
      attach( "/organizations/{labels}/labels", createServerResourceFinder( LabelsServerResource.class ) );
      attach( "/organizations/{labels}/labels/{label}", createServerResourceFinder( LabelServerResource.class ) );

      // OrganizationalUnits
      attach( "/organizations/{organization}/organizationalunits/{policy}/administrators", createServerResourceFinder( AdministratorsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}", createServerResourceFinder( OrganizationalUnitServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/groups", createServerResourceFinder( GroupsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/groups/{group}", createServerResourceFinder( GroupServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/groups/{group}/participants", createServerResourceFinder( ParticipantsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/groups/{group}/participants/{participant}", createServerResourceFinder( ParticipantServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects", createServerResourceFinder( se.streamsource.streamflow.web.resource.organizations.projects.ProjectsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{project}", createServerResourceFinder( ProjectServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{project}/members", createServerResourceFinder( MembersServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{project}/members/{member}", createServerResourceFinder( MemberServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{labels}/selectedlabels", createServerResourceFinder( SelectedLabelsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{labels}/selectedlabels/{label}", createServerResourceFinder( SelectedLabelServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{tasktypes}/tasktypes", createServerResourceFinder( SelectedTaskTypesServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/projects/{tasktypes}/tasktypes/{tasktype}", createServerResourceFinder( SelectedTaskTypeServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/administrators", createServerResourceFinder( AdministratorsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{ou}/administrators/{administrator}", createServerResourceFinder( AdministratorServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{organizationalunits}/organizationalunits", createServerResourceFinder( OrganizationalUnitsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{labels}/selectedlabels", createServerResourceFinder( SelectedLabelsServerResource.class ) );
      attach( "/organizations/{organization}/organizationalunits/{labels}/selectedlabels/{label}", createServerResourceFinder( SelectedLabelServerResource.class ) );

      // Tasks
      attach( "/tasks/{task}", createServerResourceFinder( TaskServerResource.class ) );
      attach( "/tasks/{task}/actions", createServerResourceFinder( TaskActionsServerResource.class ) );
      attach( "/tasks/{task}/general", createServerResourceFinder( TaskGeneralServerResource.class ) );
      attach( "/tasks/{task}/comments", createServerResourceFinder( TaskCommentsServerResource.class ) );
      attach( "/tasks/{task}/contacts", createServerResourceFinder( TaskContactsServerResource.class ) );
      attach( "/tasks/{task}/contacts/{index}", createServerResourceFinder( TaskContactServerResource.class ) );
      attach( "/tasks/{task}/forms", createServerResourceFinder( TaskFormsServerResource.class ) );
      attach( "/tasks/{task}/forms/{formsubmission}", createServerResourceFinder( TaskFormServerResource.class ) );

      // Events
      attach( "/events", createServerResourceFinder( EventsServerResource.class ) );

      // Qi4j
      Router qi4jRouter = new Router( getContext() );
      qi4jRouter.attach( "/entity", createServerResourceFinder( EntitiesResource.class ) );
      qi4jRouter.attach( "/entity/{identity}", createServerResourceFinder( EntityResource.class ) );
      qi4jRouter.attach( "/query", createServerResourceFinder( SPARQLResource.class ) );
      qi4jRouter.attach( "/query/index", createServerResourceFinder( IndexResource.class ) );
      attach( "/qi4j", new ExtensionMediaTypeFilter( getContext(), qi4jRouter ) );


      attach( "/admin/console", createServerResourceFinder( ConsoleServerResource.class ) );

      // Version info
      Directory directory = new Directory( getContext(), "clap://class/static/" );
      directory.setListingAllowed( true );
      attach( "/static", directory );
   }

   private Restlet createServerResourceFinder( Class<? extends ServerResource> resource )
   {
      return createServerResourceFinder( resource, true );
   }

   private Restlet createServerResourceFinder( Class<? extends ServerResource> resource, boolean secure )
   {
      ResourceFinder finder = factory.newObject( ResourceFinder.class );
      finder.setTargetClass( resource );

      if (secure)
      {
         Authenticator auth = new ChallengeAuthenticator( getContext(), ChallengeScheme.HTTP_BASIC, "StreamFlow" );
         auth.setNext( finder );
         return auth;
      } else
         return finder;
   }

   private Restlet createServerCompositeFinder( Class<? extends TransientComposite> resource )
   {
      CompositeFinder finder = factory.newObject( CompositeFinder.class );
      finder.setTargetClass( resource );

      return finder;
   }

}
