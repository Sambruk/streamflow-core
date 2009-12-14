package se.streamsource.streamflow.client.ui.menu;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.client.domain.individual.AccountEntity;

public interface AddAccountModel
      extends Composite
{

   Property<AccountEntity> account();

}
