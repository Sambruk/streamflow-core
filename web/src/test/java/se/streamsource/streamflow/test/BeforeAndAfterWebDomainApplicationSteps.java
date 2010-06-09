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

package se.streamsource.streamflow.test;

import org.jbehave.scenario.annotations.AfterScenario;
import org.jbehave.scenario.annotations.AfterStory;
import org.jbehave.scenario.annotations.BeforeScenario;
import org.jbehave.scenario.annotations.BeforeStory;
import org.jbehave.scenario.steps.CandidateStep;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationSPI;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventCollector;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionEventAdapter;
import se.streamsource.streamflow.test.GenericSteps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JAVADOC
 */
public class BeforeAndAfterWebDomainApplicationSteps
      extends Steps
{
   CandidateSteps[] steps;

   public BeforeAndAfterWebDomainApplicationSteps()
   {
   }

   public BeforeAndAfterWebDomainApplicationSteps( CandidateSteps... steps )
   {
      CandidateSteps[] theSteps = new CandidateSteps[steps.length + 1];
      System.arraycopy( steps, 0, theSteps, 0, steps.length );
      this.genericSteps = new GenericSteps();
      theSteps[steps.length] = genericSteps;
      this.steps = theSteps;

      try
      {
         Energy4Java is = new Energy4Java();

         Class[] stepClasses = new Class[theSteps.length + 1];
         stepClasses[0] = getClass();
         for (int i = 0; i < theSteps.length; i++)
         {
            CandidateSteps step = theSteps[i];
            stepClasses[i + 1] = step.getClass();
         }

//            Client restlet = new Client(Protocol.HTTP);
         app = is.newApplication( new StreamflowWebDomainTestAssembler( stepClasses, genericSteps ) );

         Module module = app.findModule( "Application", "Test" );

         for (CandidateSteps step : theSteps)
         {
            Class<CandidateSteps> aClass = (Class<CandidateSteps>) step.getClass();
            ObjectBuilder<CandidateSteps> builder = module.objectBuilderFactory().newObjectBuilder( aClass );
            builder.use( steps, eventCollector );
            builder.injectTo( step );
         }

         module.objectBuilderFactory().newObjectBuilder( BeforeAndAfterWebDomainApplicationSteps.class ).injectTo( this );
      } catch (AssemblyException e)
      {
         e.printStackTrace();
      }
   }

   @Structure
   protected UnitOfWorkFactory uowf;

   @Structure
   protected Qi4jSPI spi;

   @Service
   protected EventSource eventSource;

   protected ApplicationSPI app;
   protected UnitOfWork uow;

   private GenericSteps genericSteps;

   EventCollector eventCollector = new EventCollector();

   @BeforeStory
   public void newApplication() throws Exception
   {
      app.activate();

      eventSource.registerListener( new TransactionEventAdapter(eventCollector));
   }

   @BeforeScenario
   public void newUnitOfWork() throws Exception
   {
      uow = uowf.newUnitOfWork();

      eventCollector.events().clear();
   }

   @Override
   public CandidateStep[] getSteps()
   {
      List<CandidateStep> stepList = new ArrayList<CandidateStep>();
      for (int i = 0; i < steps.length; i++)
      {
         CandidateSteps step = steps[i];
         stepList.addAll( Arrays.asList( step.getSteps() ) );
      }
      return stepList.toArray( new CandidateStep[stepList.size()] );
   }

   @AfterScenario
   public void completeUnitOfWork() throws Exception
   {

      if (uow.isOpen())
         uow.complete();
   }

   @AfterStory
   public void closeApplication() throws Exception
   {
      app.passivate();
   }
}