package se.streamsource.streamflow.client.ui.administration.filters;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;

/**
 * Email filter action
 */
public class EmailActionView
   extends JPanel
{
   public EmailActionView(@Service ApplicationContext context, @Uses EmailActionModel model)
   {
      JLabel label = new JLabel(i18n.text(AdministrationResources.send_email_to, model.getIndex().getFirstValue("recipient")), JLabel.LEFT);
      add(label);
   }
}
