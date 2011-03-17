package se.streamsource.streamflow.domain.contact;

import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.List;

/**
 * Builder for ContactValue. Can be used during creation or updating of existing contact
 */
public class ContactBuilder
{
   private ValueBuilder<ContactValue> contactBuilder;
   private ValueBuilderFactory vbf;

   public ContactBuilder(ContactValue contact, ValueBuilderFactory vbf)
   {
      this.vbf = vbf;
      contactBuilder =contact.buildWith();
   }

   public ContactBuilder(ValueBuilderFactory vbf)
   {
      this.vbf = vbf;
      contactBuilder = vbf.newValueBuilder(ContactValue.class);
   }

   public ContactBuilder name(String name)
   {
      contactBuilder.prototype().name().set(name);
      return this;
   }

   public ContactBuilder company(String company)
   {
      contactBuilder.prototype().company().set(company);
      return this;
   }

   public ContactBuilder note(String note)
   {
      contactBuilder.prototype().note().set(note);
      return this;
   }

   public ContactBuilder phoneNumber(String phoneNumber)
   {
      List<ContactPhoneValue> phoneNumbers = contactBuilder.prototype().phoneNumbers().get();
      phoneNumbers.clear();
      ValueBuilder<ContactPhoneValue> phoneBuilder = vbf.newValueBuilder(ContactPhoneValue.class);
      phoneBuilder.prototype().phoneNumber().set(phoneNumber);
      phoneNumbers.add(phoneBuilder.newInstance());

      return this;
   }

   public ContactBuilder address(String address)
   {
      List<ContactAddressValue> addresses = contactBuilder.prototype().addresses().get();
      addresses.clear();
      ValueBuilder<ContactAddressValue> addressBuilder = vbf.newValueBuilder(ContactAddressValue.class);
      addressBuilder.prototype().address().set(address);
      addresses.add(addressBuilder.newInstance());

      return this;
   }

   public ContactBuilder email(String email)
   {
      List<ContactEmailValue> addresses = contactBuilder.prototype().emailAddresses().get();
      addresses.clear();
      ValueBuilder<ContactEmailValue> addressBuilder = vbf.newValueBuilder(ContactEmailValue.class);
      addressBuilder.prototype().emailAddress().set(email);
      addresses.add(addressBuilder.newInstance());

      return this;
   }

   public ContactValue newInstance()
   {
      return contactBuilder.newInstance();
   }
}
