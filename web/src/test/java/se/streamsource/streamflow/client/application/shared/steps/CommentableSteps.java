/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.application.shared.steps;

import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.interaction.comment.CommentValue;

import java.util.Date;

/**
 * JAVADOC
 */
public class CommentableSteps
      extends Steps
{
   @Uses
   InboxSteps inboxSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   @Structure
   ValueBuilderFactory vbf;

   @When("comment with text $comment is added")
   public void comment( String comment )
   {
      ValueBuilder<CommentValue> builder = vbf.newValueBuilder( CommentValue.class );
      builder.prototype().text().set( comment );
      builder.prototype().creationDate().set( new Date() );
      builder.prototype().commenter().set( EntityReference.getEntityReference( orgsSteps.givenUser ) );

      inboxSteps.givenTask.addComment( builder.newInstance() );
   }
}