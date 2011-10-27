package se.streamsource.streamflow.web.rest.resource.contactlookup;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.context.contactlookup.ContactLookupContext;

/**
 * Resource for contact lookup used mostly by reference plugin implementation.
 */
public class ContactLookupResource
   extends CommandQueryResource
{

   public ContactLookupResource()
   {
      super( ContactLookupContext.class );
   }

    public ContactList contactlookup( ContactValue template )
   {
      return context(  ContactLookupContext.class ).contactlookup( template );
   }
}
