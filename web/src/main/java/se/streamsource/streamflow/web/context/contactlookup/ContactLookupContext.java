package se.streamsource.streamflow.web.context.contactlookup;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;

/**
 * The context for the contact lookup used mainly by the reference plugin implementation.
 */
public class ContactLookupContext
{
   @Structure
   Module module;

   @Optional
   @Service
   StreamflowContactLookupService contactLookup;

   @ServiceAvailable(StreamflowContactLookupService.class)
   public ContactList contactlookup( ContactValue template )
   {
      if (contactLookup != null)
         return contactLookup.lookup( template );
      else
         return module.valueBuilderFactory().newValue( ContactList.class );
   }
}
