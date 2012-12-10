package se.streamsource.streamflow.web.domain.structure.task;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;

public interface DoubleSignatureTasks
{

   interface Data {
      
      @UseDefaults
      @Queryable(false)
      ManyAssociation<DoubleSignatureTask> doubleSignatureTasks();
   }
}
