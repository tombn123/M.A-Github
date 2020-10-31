package MAGit;

import Objects.Commit;
import System.FolderDifferences;

public class CommitDifferences {
    Commit m_AncestorCommit;
    Commit m_ChildCommit;
    FolderDifferences rootFoldersDifferences;

    public CommitDifferences(Commit i_AncestorCommit, Commit i_ChildCommit, FolderDifferences i_difference) {
        this.m_AncestorCommit = i_AncestorCommit;
        this.m_ChildCommit = i_ChildCommit;
        this.rootFoldersDifferences = i_difference;
    }
}
