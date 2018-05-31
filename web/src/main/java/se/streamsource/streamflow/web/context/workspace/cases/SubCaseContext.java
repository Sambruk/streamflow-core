package se.streamsource.streamflow.web.context.workspace.cases;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.workspace.CaseSearchResult;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.SubCase;

import java.util.Collections;

/**
 * Created by dmizem from Ubrainians for imCode on 16.05.18.
 */
public class SubCaseContext implements IndexContext<CaseSearchResult> {
    public CaseSearchResult index() {
        return new CaseSearchResult(Collections.singleton(RoleMap.role(SubCase.Data.class).parent().get()));
    }

    @RequiresPermission(PermissionType.write)
    public void changeparent(Case newParent) {
        RoleMap.role(SubCase.class).changeParent(newParent);
    }
}