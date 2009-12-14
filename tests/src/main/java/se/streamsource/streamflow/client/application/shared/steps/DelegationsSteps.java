package se.streamsource.streamflow.client.application.shared.steps;

import org.hamcrest.CoreMatchers;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.delegation.DelegationsTaskListDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.web.domain.task.TaskEntity;

import static org.jbehave.Ensure.*;

public class DelegationsSteps extends Steps
{
   @Structure
   UnitOfWorkFactory uowf;

   @Uses
   ProjectsSteps projectsSteps;

   @Uses
   OrganizationsSteps orgsSteps;

   @Uses
   GenericSteps genericSteps;

   public TaskEntity givenTask;

   @Given("first delegated task")
   public void givenDelegatedTask() throws UnitOfWorkCompletionException
   {
      uowf.currentUnitOfWork().apply();
      DelegationsTaskListDTO list = projectsSteps.givenProject.delegationsTasks();
      TaskDTO task = list.tasks().get().get( 0 );
      givenTask = uowf.currentUnitOfWork().get( TaskEntity.class, task.task().get().identity() );
   }

   @When("delegated task is marked as $mark")
   public void markDelegatedTaskAs( String mark )
   {
      if ("read".equals( mark ))
      {
         projectsSteps.givenProject.markDelegatedTaskAsRead( givenTask );
      } else
      {
         projectsSteps.givenProject.markDelegatedTaskAsUnread( givenTask );
      }
   }

   @When("delegated task is finished")
   public void finishDelegatedTask()
   {
      projectsSteps.givenProject.finishDelegatedTask( givenTask,
            orgsSteps.givenUser );
   }

   @Then("task is done")
   public void taskStatusEqualsDone()
   {
      ensureThat( givenTask.status().get(), CoreMatchers
            .equalTo( TaskStates.DONE ) );
   }

}
