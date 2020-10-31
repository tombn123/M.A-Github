package collaboration;

import Objects.Commit;
import Objects.Folder;
import Objects.branch.Branch;
import Objects.branch.BranchUtils;
import System.Engine;
import System.Users.User;
import XmlObjects.repositoryWriters.LocalRepositoryWriter;
import XmlObjects.repositoryWriters.RepositoryWriter;
import common.constants.ResourceUtils;

import java.io.IOException;
import java.text.ParseException;

public class Push
{
    private Fetch m_Fetcher;
    private Engine m_Engine;
    private LocalRepository m_LocalRepository;
    private Branch m_BranchToPushToInRepo;
    private RemoteBranch m_RemoteBranchOfRTB;

    public Push(Engine engine, LocalRepository localRepository) throws Exception
    {
        m_LocalRepository = localRepository;
        m_Engine = engine;
        m_Fetcher = new Fetch(engine, localRepository);
    }


    public boolean isPossibleToPush(User loggedInUser) throws Exception
    {
        if (m_Engine.ShowStatus(loggedInUser) != null)
            return false;

        //check if active branch is RTB
        if (!BranchUtils.IsRemoteTrackingBranch(m_LocalRepository.getActiveBranch()))
            return false;

        //headbranch in local is RTB
        RemoteTrackingBranch remoteTrackingBranchInLocal = (RemoteTrackingBranch) m_LocalRepository.getActiveBranch();
        String remoteBranchNameExpected = m_LocalRepository.getRemoteRepoRef().getName() + ResourceUtils.Slash +
                remoteTrackingBranchInLocal.getBranchName();

        m_RemoteBranchOfRTB = m_LocalRepository.findRemoteBranchByPredicate(
                remoteBranch ->
                        remoteBranch.getBranchName().equals(remoteBranchNameExpected)
        );

        m_BranchToPushToInRepo = m_Fetcher.getRemoteRepositoryToFetchFrom().findBranchByPredicate(branch ->
                branch.getBranchName().equals(remoteTrackingBranchInLocal.getBranchName()));

        //checking if the remoteBranch in local and the branch in remote repository in the same location
        return m_RemoteBranchOfRTB.getPointedCommit().AreTheCommitsTheSame(m_BranchToPushToInRepo.getPointedCommit()) &&
                !remoteTrackingBranchInLocal.getPointedCommit().AreTheCommitsTheSame(m_BranchToPushToInRepo.getPointedCommit());
        //checking if there is something to pull
    }

    public void Push() throws IOException, ParseException
    {
        RepositoryWriter repositoryWriter = new RepositoryWriter(m_Fetcher.getRemoteRepositoryToFetchFrom());
        LocalRepositoryWriter localRepositoryWriter = new LocalRepositoryWriter(m_LocalRepository);


        //in function isPossibleToPush we checked that headBranch Is rtb
        m_BranchToPushToInRepo.setPointedCommit(m_LocalRepository.getActiveBranch().getPointedCommit());
        m_RemoteBranchOfRTB.setPointedCommit(m_LocalRepository.getActiveBranch().getPointedCommit());
        localRepositoryWriter.WriteRemoteBranch(m_RemoteBranchOfRTB);

        repositoryWriter.WriteBranch(m_BranchToPushToInRepo);

        //check if needed to do checkout in remote repository
        if (m_BranchToPushToInRepo.AreTheSameBranches(m_LocalRepository.getActiveBranch()))
        {
            Commit commitOfHeadBranchInRepoDup = new Commit(m_BranchToPushToInRepo.getPointedCommit());
            commitOfHeadBranchInRepoDup.getRootFolder().initFolderPaths(m_Fetcher.getRemoteRepositoryToFetchFrom().getRepositoryPath());

            Folder.RemoveFilesAndFoldersWithoutMagit(commitOfHeadBranchInRepoDup.getRootFolder().GetPath());
            Folder.SpanDirectory(m_BranchToPushToInRepo.getPointedCommit().getRootFolder());
        }
    }
}
