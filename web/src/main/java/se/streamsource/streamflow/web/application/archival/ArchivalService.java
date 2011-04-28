package se.streamsource.streamflow.web.application.archival;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.ArchivalSettings;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import java.util.Calendar;
import java.util.Date;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * TODO
 */
@Mixins(ArchivalService.Mixin.class)
public interface ArchivalService
      extends ServiceComposite, Configuration, Activatable
{
   void performArchivalCheck();

   abstract class Mixin
         implements ArchivalService
   {
      @This
      Configuration<ArchivalConfiguration> config;

      @Structure
      Module module;

      Usecase archivalCheck = UsecaseBuilder.newUsecase("Archival check");

      Logger logger = LoggerFactory.getLogger(ArchivalService.class);

      public void activate() throws Exception
      {

      }

      public void passivate() throws Exception
      {

      }

      public void performArchivalCheck()
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(archivalCheck);

         try
         {
            Property<Integer> maxAge = templateFor(ArchivalSettings.Data.class).maxAge();
            Query<ArchivalSettings.Data> settings = module.queryBuilderFactory().newQueryBuilder(ArchivalSettings.Data.class).where(notEq(maxAge, 0)).newQuery(uow);

            for (ArchivalSettings.Data setting : settings)
            {
               Calendar calendar = Calendar.getInstance();
               calendar.add(Calendar.DAY_OF_MONTH, -setting.maxAge().get());
               Date maxAgeDate = calendar.getTime();

               Query<CaseEntity> cases = module.queryBuilderFactory().
                     newQueryBuilder(CaseEntity.class).
                     where(and(eq(templateFor(Status.Data.class).status(), CaseStates.CLOSED),
                           lt(QueryExpressions.templateFor(CreatedOn.class).createdOn(), maxAgeDate))).newQuery(uow);

               for (CaseEntity caseEntity : cases)
               {
                  logger.info("Case " + caseEntity.getDescription() + "(" + caseEntity.caseId() + "), created on " + caseEntity.createdOn().get() + ", can be archived");
               }
            }
         } finally
         {
            uow.discard();
         }
      }
   }
}
