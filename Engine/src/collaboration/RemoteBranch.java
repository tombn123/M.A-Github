package collaboration;

import Objects.Commit;
import Objects.branch.Branch;
import common.constants.ResourceUtils;

import java.io.File;

public class RemoteBranch extends Branch
{
    public RemoteBranch(String i_BranchName, Commit i_CurrentCommit)
    {
        super(i_BranchName, i_CurrentCommit);
    }

    public static RemoteBranch CreateRemoteBranchFromBranch(Branch i_Branch, String i_CloneFromRepoName)
    {
        String remoteBranchName = GetRemoteBranchName(i_Branch.getBranchName(), i_CloneFromRepoName);

        return new RemoteBranch(remoteBranchName, i_Branch.getPointedCommit());
    }

    public static String GetRemoteBranchName(String i_BranchName, String i_CloneFromRepoName)
    {
        return i_CloneFromRepoName + ResourceUtils.Slash + i_BranchName;
    }
}
