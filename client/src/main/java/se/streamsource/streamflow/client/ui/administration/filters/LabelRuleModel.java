package se.streamsource.streamflow.client.ui.administration.filters;

import org.restlet.data.Form;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * TODO
 */
public class LabelRuleModel
   extends ResourceModel<Form>
{
   @Override
   public Form getIndex()
   {
      return client.query("index", Form.class);
   }
}
