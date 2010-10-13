package se.streamsource.streamflow.server.plugin.authentication;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Describes the identity of a user with username and password.
 */
public interface UserIdentityValue extends ValueComposite
{
   @UseDefaults
   Property<String> username();

   @UseDefaults
   Property<String> password();
}
