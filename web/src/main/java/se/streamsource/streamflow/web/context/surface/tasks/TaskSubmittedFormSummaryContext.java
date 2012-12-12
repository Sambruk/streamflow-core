package se.streamsource.streamflow.web.context.surface.tasks;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;

import static se.streamsource.dci.api.RoleMap.*;

/**
 *
 * */

@Mixins( TaskSubmittedFormSummaryContext.Mixin.class )
public interface TaskSubmittedFormSummaryContext
   extends Context, IndexContext<SubmittedFormValue>
{
   abstract class Mixin
      implements TaskSubmittedFormSummaryContext
   {
      public SubmittedFormValue index()
      {
         return role( SubmittedFormValue.class );
      }
   }
}
