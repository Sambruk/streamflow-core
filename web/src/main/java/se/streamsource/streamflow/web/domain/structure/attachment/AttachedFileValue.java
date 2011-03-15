package se.streamsource.streamflow.web.domain.structure.attachment;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * TODO
 */
public interface AttachedFileValue
   extends ValueComposite
{
   Property<String> name();

   @Optional
   Property<String> mimeType();

   Property<String> uri();

   Property<Date> modificationDate();

   Property<Long> size();
}
