package se.streamsource.streamflow.web.context.workspace.cases;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.SubCase;

/**
 * Created by dmizem from Ubrainians for imCode on 16.05.18.
 */
public class SubCaseContext implements IndexContext<Case> {
    public Case index() {
        return RoleMap.role(SubCase.Data.class).parent().get();
    }

}