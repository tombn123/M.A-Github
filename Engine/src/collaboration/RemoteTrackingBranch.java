package collaboration;

import Objects.branch.Branch;
import Objects.Commit;

public class RemoteTrackingBranch extends Branch
{
    public RemoteTrackingBranch(String i_BranchName, Commit i_CurrentCommit)
    {
        super(i_BranchName, i_CurrentCommit);
    }

    public RemoteTrackingBranch(Branch i_ActiveBranch)
    {
        super(i_ActiveBranch.getBranchName(), i_ActiveBranch.getPointedCommit());
    }
}
