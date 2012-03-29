package se.streamsource.streamflow.client.ui.administration.casesettings;

import org.restlet.data.Form;
import se.streamsource.streamflow.api.administration.form.FormValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * Model behind CasePrioritySettingView
 */
public class CasePrioritySettingModel
   extends ResourceModel<FormValue>
{
   public void changeCasePrioritySetting( Boolean visible, Boolean mandatory )
   {
      Form form = new Form();
      form.set( "visible", visible.toString() );
      form.set( "mandatory", mandatory.toString() );

      client.postLink(command("update"), form);
   }
}
