package se.streamsource.streamflow.client.ui.administration.surface;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * TODO
 */
public class EmailAccessPointView
        extends JPanel
        implements TransactionListener
{
   public EmailAccessPointView(@Uses CommandQueryClient client)
   {
      FormLayout layout = new FormLayout(
              "75dlu, 5dlu, 120dlu", "pref, pref, pref");
      DefaultFormBuilder formBuilder = new DefaultFormBuilder(layout, this);

      EmailAccessPointValue emailAccessPoint = client.query("index", EmailAccessPointValue.class);

      formBuilder.append(i18n.text(AdministrationResources.email), new JLabel(emailAccessPoint.email().get()));
      formBuilder.nextLine();
      formBuilder.append(i18n.text(AdministrationResources.accesspoint));
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}
