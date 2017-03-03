package se.streamsource.streamflow.web.application.entityexport;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
@Mixins(EntityExportJob.Mixin.class)
public interface EntityExportJob extends Job, TransientComposite
{

   abstract class Mixin
           implements EntityExportJob
   {
      private final Logger logger = LoggerFactory.getLogger( EntityStateChangeListener.class );

      @Service
      private EntityExportService entityExportService;

      @Override
      public void execute( JobExecutionContext context ) throws JobExecutionException
      {
         try
         {

         } catch ( Exception e )
         {
            throw new JobExecutionException( e );
         }

      }


   }


}
