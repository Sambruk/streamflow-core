package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.client.util.SuggestModel;
import se.streamsource.streamflow.client.util.SuggestTextField;

public class FormStreetAddressSuggestTextField extends SuggestTextField<StreetSearchDTO>
{
   private static final long serialVersionUID = -227081535521942025L;

   public FormStreetAddressSuggestTextField(SuggestModel<StreetSearchDTO> model)
   {
      super( model );
   }

   @Override
   public void handleSaveAction(String text)
   {
      // TODO Auto-generated method stub

   }

}
