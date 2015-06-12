package se.streamsource.streamflow.web.context.util;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

import se.streamsource.dci.value.link.LinksValue;

public interface SearchResultDTO extends LinksValue {
   @UseDefaults
   Property<Integer> unlimitedResultCount();
}
