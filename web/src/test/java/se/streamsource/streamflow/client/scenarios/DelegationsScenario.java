package se.streamsource.streamflow.client.scenarios;

import se.streamsource.streamflow.client.application.shared.steps.DelegationsSteps;
import se.streamsource.streamflow.client.application.shared.steps.FieldDefinitionsSteps;
import se.streamsource.streamflow.client.application.shared.steps.FormsSteps;
import se.streamsource.streamflow.client.application.shared.steps.GroupsSteps;
import se.streamsource.streamflow.client.application.shared.steps.MembersSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationalUnitsSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ParticipantsSteps;
import se.streamsource.streamflow.client.application.shared.steps.ProjectsSteps;
import se.streamsource.streamflow.client.application.shared.steps.TaskTypesSteps;
import se.streamsource.streamflow.client.application.shared.steps.UserSteps;
import se.streamsource.streamflow.client.application.shared.steps.setup.TestSetupSteps;
import se.streamsource.streamflow.test.AbstractWebDomainApplicationScenario;

public class DelegationsScenario extends AbstractWebDomainApplicationScenario
{
   public DelegationsScenario()
   {
      this( Thread.currentThread().getContextClassLoader() );
   }

   public DelegationsScenario( ClassLoader classLoader )
   {
      super( classLoader, new TestSetupSteps(),
            new FieldDefinitionsSteps(),
            new FormsSteps(),
            new OrganizationsSteps(),
            new OrganizationalUnitsSteps(),
            new MembersSteps(),
            new TaskTypesSteps(),
            new ProjectsSteps(),
            new ParticipantsSteps(),
            new GroupsSteps(),
            new UserSteps(),
            new DelegationsSteps()
      );
   }
}
