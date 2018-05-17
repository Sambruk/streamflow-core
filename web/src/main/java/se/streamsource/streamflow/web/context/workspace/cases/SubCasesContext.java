package se.streamsource.streamflow.web.context.workspace.cases;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.workspace.CaseSearchResult;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.SubCases;

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;

/**
 * Created by dmizem from Ubrainians for imCode on 15.05.18.
 */
public class SubCasesContext implements IndexContext<CaseSearchResult> {

    public CaseSearchResult index() {
        return new CaseSearchResult(RoleMap.role(SubCases.Data.class).subCases().toList());
    }

    @RequiresStatus(OPEN)
    @RequiresPermission(PermissionType.write)
    public void createsubcase() {
        RoleMap.role(SubCases.class).createSubCase();
    }

    @RequiresPermission(PermissionType.write)
    public void removesubCase(Case subCase) {
        RoleMap.role(SubCases.class).removeSubCase(subCase);
    }
}