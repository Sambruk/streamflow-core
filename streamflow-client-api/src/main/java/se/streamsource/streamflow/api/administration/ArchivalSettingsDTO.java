package se.streamsource.streamflow.api.administration;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Range;

/**
 * TODO
 */
public interface ArchivalSettingsDTO
   extends ValueComposite
{
   enum ArchivalType
   {
      delete,
      export
   }

   @UseDefaults
   @Range(min = 0, max = Double.MAX_VALUE)
   Property<Integer> maxAge();

   @UseDefaults
   Property<ArchivalType> archivalType();
}
