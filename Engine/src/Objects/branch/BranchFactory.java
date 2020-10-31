package Objects.branch;

import Objects.Commit;
import collaboration.RemoteBranch;
import collaboration.RemoteTrackingBranch;
import common.Enums;

import java.util.List;

public class BranchFactory
{
    public static Branch CreateBranchInBranchFactory(List<Branch> branches, List<RemoteTrackingBranch> remoteTrackingBranches,
                                                     List<RemoteBranch> remoteBranches, String branchName, Commit currentCommit,
                                                     Enums.BranchType type)
    {
      //  Enums type = analyzeBranchType(magitSingleBranch);
        Branch currentBranch;

        switch (type)
        {
            case REMOTE_BRANCH:
                currentBranch = new RemoteBranch(branchName, currentCommit);
                remoteBranches.add((RemoteBranch) currentBranch);
                return currentBranch;
            //break;

            case REMOTE_TRACKING_BRANCH:
                currentBranch = new RemoteTrackingBranch(branchName, currentCommit);
                remoteTrackingBranches.add((RemoteTrackingBranch) currentBranch);
                return currentBranch;
            //checkIfCurrentBranchIsHEAD(magitSingleBranch, currentBranch);
            //break;

            default:// case BRANCH:
                currentBranch = new Branch(branchName, currentCommit);
                branches.add(currentBranch);
                return currentBranch;
            //checkIfCurrentBranchIsHEAD(magitSingleBranch, currentBranch);
            //break;
        }
    }

    /*private static Enums analyzeBranchType(MagitSingleBranch i_MagitSingleBranch)
    {
        if (i_MagitSingleBranch.isRemote != null)
        {
            return Enums.REMOTE_BRANCH;
        }

        return i_MagitSingleBranch.tracking != null ?
                Enums.REMOTE_TRACKING_BRANCH :
                Enums.BRANCH;
    }*/

   /* public enum Enums
    {
        REMOTE_BRANCH, REMOTE_TRACKING_BRANCH, BRANCH
    }*/
}
