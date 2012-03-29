package se.streamsource.streamflow.api.administration;

import java.util.List;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Range;

public interface DueOnNotificationSettingsDTO
   extends ValueComposite
{
   @UseDefaults
   @Range(min = 0, max = Double.MAX_VALUE)
   Property<Integer> threshold();

   @UseDefaults
   Property<Boolean> active();
   
   @UseDefaults
   Property<List<EntityReference>> additionalrecievers();
}
