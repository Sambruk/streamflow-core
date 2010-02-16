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

      attach(new ExtensionMediaTypeFilter( getContext(), createServerResourceFinder( DCICommandQueryServerResource.class )));

//      attach( createServerResourceFinder( StreamFlowServerResource.class ) );
/*
      // Users
      attach( "/users", createServerResourceFinder( UsersServerResource.class, false ) );

      UserAccessFilter userFilter = factory.newObject( UserAccessFilter.class );
      userFilter.setNext( factory.newObjectBuilder( UsersRouter.class ).use( context ).newInstance() );
      attach( "/users/{user}", userFilter );

      attach( "/users/{labels}/workspace/user/labels", createServerResourceFinder( LabelsServerResource.class ) );
      attach( "/users/{labels}/workspace/user/labels/{label}", createServerResourceFinder( LabelServerResource.class ) );

//      attach ("/users", createServerResourceFinder( DCICommandQueryServerResource.class ));

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
      attach( "/organizations/{organization}/tasktypes/{forms}/forms/{form}/pages/{page}/fields/{field}", createServerResourceFinder( FormDefinitionFieldServerResource.class ) );
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
x      attach( "/tasks/{task}", createServerResourceFinder( TaskServerResource.class ) );
x      attach( "/tasks/{task}/actions", createServerResourceFinder( TaskActionsServerResource.class ) );
x      attach( "/tasks/{task}/general", createServerResourceFinder( TaskGeneralServerResource.class ) );
x      attach( "/tasks/{task}/comments", createServerResourceFinder( TaskCommentsServerResource.class ) );
x      attach( "/tasks/{task}/contacts", createServerResourceFinder( TaskContactsServerResource.class ) );
x      attach( "/tasks/{task}/contacts/{index}", createServerResourceFinder( TaskContactServerResource.class ) );
x      attach( "/tasks/{task}/forms", createServerResourceFinder( TaskFormsServerResource.class ) );
x      attach( "/tasks/{task}/forms/{formsubmission}", createServerResourceFinder( TaskFormServerResource.class ) );
      */
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
}
