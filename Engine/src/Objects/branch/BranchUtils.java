package Objects.branch;

import collaboration.RemoteBranch;
import collaboration.RemoteTrackingBranch;

public class BranchUtils
{
    public final static String RTB_STYLE = "#ff2121";
    public final static String RB_STYLE = "##1e90ff";


    public static boolean IsRemoteTrackingBranch(Branch branch)
    {
        return branch.getClass().equals(RemoteTrackingBranch.class);
    }

    public static boolean IsRemoteBranch(Branch branch)
    {
        return branch.getClass().equals(RemoteBranch.class);
    }
}
