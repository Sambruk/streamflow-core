package se.streamsource.streamflow.web.domain.entity.task;

import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;

public interface DoubleSignatureTaskEntity 
   extends 
      DoubleSignatureTask,
      DoubleSignatureTask.Data,
      DomainEntity
{

}
