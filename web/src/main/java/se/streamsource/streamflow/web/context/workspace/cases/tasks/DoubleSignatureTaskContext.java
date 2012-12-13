package se.streamsource.streamflow.web.context.workspace.cases.tasks;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

import static se.streamsource.dci.api.RoleMap.*;

/**
 *
 */
@Mixins(DoubleSignatureTaskContext.Mixin.class)
public interface DoubleSignatureTaskContext
   extends Context
{

   void sendemail();

   abstract class Mixin
      implements DoubleSignatureTaskContext
   {
      @Optional
      @Service
      MailSenderService mailSender;

      public void sendemail()
      {
         mailSender.sentEmail( role( DoubleSignatureTask.Data.class ).email().get() );
      }
   }
}
