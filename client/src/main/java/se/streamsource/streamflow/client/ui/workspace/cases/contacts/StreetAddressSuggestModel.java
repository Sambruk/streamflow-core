package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import java.util.List;

import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;
import se.streamsource.streamflow.client.util.SuggestModel;


public class StreetAddressSuggestModel implements SuggestModel<StreetSearchDTO>
{
   private ContactModel contactModel;
   private StreetsDTO streets;

   public List<StreetSearchDTO> options(String searchString)
   {
      streets = contactModel.searchStreets( searchString );
      return streets.streets().get();
   }

   public String displayValue(StreetSearchDTO value)
   {
      return value.address().get() + ", " + value.area().get();
   }

   public String displayValueAt(int index)
   {
      return displayValue( streets.streets().get().get( index ) );
   }

   public void setContactModel(ContactModel contactModel)
   {
      this.contactModel = contactModel;
   }
}
