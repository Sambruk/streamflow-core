package se.streamsource.streamflow.web.domain.entity.organization;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;

/**
 * The entity describing priority.
 */
public interface PriorityEntity
   extends DomainEntity,
      Priority,

      // Interactions
      IdGenerator,

      // Data
      Describable.Data,
      IdGenerator.Data,
      Removable.Data
{
}
