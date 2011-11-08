package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import javax.swing.JTextField;

import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.client.util.SuggestTextField;
import se.streamsource.streamflow.client.util.ValueBinder;

/**
 * Specific Class to handle the live search for Street Names. It should also
 * update the City attribute which explains the need for this class.
 * 
 * @author henrikreinhold
 *
 * @param <T>
 */
public class StreetAddressSuggestTextField extends SuggestTextField<StreetSearchDTO>
{

   private static final long serialVersionUID = -1952912369783423979L;
   private final JTextField cityField;
   private StreetAddressSuggestModel model;
   private final ValueBinder addressViewBinder;

   public StreetAddressSuggestTextField(StreetAddressSuggestModel model, JTextField cityField, ValueBinder addressViewBinder )
   {
      super( model );
      this.cityField = cityField;
      this.model = model;
      this.addressViewBinder = addressViewBinder;
      
   }

   public void handleAcceptAction(StreetSearchDTO selectedItem) 
   {
      getTextField().setText( selectedItem.address().get() );
      cityField.setText( selectedItem.area().get() );
      model.getContactModel().getAddress().address().set( selectedItem.address().get() );
      model.getContactModel().getAddress().city().set( selectedItem.area().get() );
      addressViewBinder.update( model.getContactModel().getAddress() );
      model.getContactModel().changeAddressAndCity( selectedItem.address().get(), selectedItem.area().get() );
   };

}
