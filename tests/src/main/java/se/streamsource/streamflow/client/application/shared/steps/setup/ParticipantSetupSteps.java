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

package se.streamsource.streamflow.client.application.shared.steps.setup;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.web.domain.group.Participant;

/**
 * JAVADOC
 */
public class ParticipantSetupSteps
        extends Steps
{
    @Uses
    UserSetupSteps userSetup;

    @Uses
    ProjectsSetupSteps projectSetup;

    @Uses
    GroupsSetupSteps groupSetup;

    @Uses
    GenericSteps genericSteps;

    public Participant participant;
    public Participant nonParticipant;

    @Given("basic participant setup")
    public void setupGroups() throws Exception
    {
        projectSetup.setupProjects();
        groupSetup.setupGroups();

        nonParticipant = userSetup.userMap.get("user1");
        participant = userSetup.userMap.get("user2");

        genericSteps.clearEvents();
    }
}