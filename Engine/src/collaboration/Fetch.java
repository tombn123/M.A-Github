package collaboration;

import Objects.Commit;
import Objects.branch.Branch;
import System.Engine;
import System.Repository;
import XmlObjects.repositoryWriters.LocalRepositoryWriter;
import common.constants.NumConstants;
import common.constants.ResourceUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class Fetch
{
    private Engine m_Engine;
    private LocalRepository m_CurrentLocalRepository;
    private Map<String, Commit> m_AllCommitsInLocal;
    private Repository m_RemoteRepositoryToFetchFrom;

    public Fetch(Engine i_Engine, LocalRepository i_CurrentLocalRepository) throws Exception
    {
        m_Engine = i_Engine;
        m_CurrentLocalRepository = i_CurrentLocalRepository;
        m_AllCommitsInLocal = m_CurrentLocalRepository.getAllCommitsSHA1ToCommit();

        RemoteRepositoryRef remoteRepositoryRef = m_CurrentLocalRepository.getRemoteRepoRef();

        m_Engine.PullAnExistingRepository(remoteRepositoryRef.getRepoPath().toString(),
                remoteRepositoryRef.getName());

        m_RemoteRepositoryToFetchFrom = m_Engine.getCurrentRepository();

        reassignValuesOfRepositories();
    }

    private void reassignValuesOfRepositories()
    {
        m_Engine.setCurrentLocalRepository(m_CurrentLocalRepository);
        m_Engine.setCurrentRepository(null);
    }

    public Repository getRemoteRepositoryToFetchFrom()
    {
        return m_RemoteRepositoryToFetchFrom;
    }

    public void FetchAllObjects() throws IOException, ParseException
    {
        for (Branch branch : m_RemoteRepositoryToFetchFrom.getAllBranches())
        {
            FetchBranch(branch);
        }
        LocalRepositoryWriter writer = new LocalRepositoryWriter(m_CurrentLocalRepository);
        writer.WriteAllRemoteBranches();
    }

    public void FetchBranch(Branch i_Branch)
    {
        if (!isBranchExistInLocal(i_Branch))
        {
            createCommitsAndConcatThem(i_Branch.getPointedCommit());
            m_CurrentLocalRepository.addRemoteBranch(new RemoteBranch(i_Branch.getBranchName(), i_Branch.getPointedCommit()));
        } else
        {
            //delete function and use one on LocalRepository class
            RemoteBranch remoteBranch = findRemoteBranch(m_CurrentLocalRepository.getRemoteBranches(), i_Branch);

            //if the pointed commits are not the same
            if (!remoteBranch.getPointedCommit().AreTheCommitsTheSame(i_Branch.getPointedCommit()))
            {
                createCommitsAndConcatThem(i_Branch.getPointedCommit());

                Commit parallelCommit = getParallelCommit(i_Branch.getPointedCommit());
                remoteBranch.setPointedCommit(parallelCommit);
            }
        }
    }

    public Commit getParallelCommit(Commit i_CommitInRemote)
    {
        return m_AllCommitsInLocal.get(i_CommitInRemote.getSHA1());
    }


    private void createCommitsAndConcatThem(Commit branchCommit)
    {
        if (branchCommit.ThereIsPrevCommit(NumConstants.ONE))
        {
            if (!m_AllCommitsInLocal.containsKey(branchCommit.GetPrevCommit().getSHA1()))
                createCommitsAndConcatThem(branchCommit.GetPrevCommit());

            Commit newCommitToLocal = new Commit(branchCommit);
            m_AllCommitsInLocal.put(newCommitToLocal.getSHA1(), newCommitToLocal);

            Commit prevParallelCommit = getParallelCommit(branchCommit.GetPrevCommit());
            newCommitToLocal.setPrevCommit(prevParallelCommit);
        }

        if (branchCommit.ThereIsPrevCommit(NumConstants.TWO))
        {
            if (!m_AllCommitsInLocal.containsKey(branchCommit.GetSecondPrevCommit().getSHA1()))
                createCommitsAndConcatThem(branchCommit.GetSecondPrevCommit());

            Commit newCommitToLocal = new Commit(branchCommit);
            m_AllCommitsInLocal.put(newCommitToLocal.getSHA1(), newCommitToLocal);

            Commit secondPrevParallelCommit = getParallelCommit(branchCommit.GetSecondPrevCommit());
            newCommitToLocal.setPrevCommit(secondPrevParallelCommit);
        }
    }

    /*public boolean areTheCommitsTheSame(Commit remoteBranchCommit, Commit branchCommit)
    {
        return branchCommit.getSHA1().equals(remoteBranchCommit.getSHA1());
    }*/

    private RemoteBranch findRemoteBranch(List<RemoteBranch> i_RemoteBranches, Branch i_Branch)
    {
        String remoteBranchName = m_CurrentLocalRepository.getRemoteRepoRef().getName() + "/" + i_Branch.getBranchName();

        return i_RemoteBranches
                .stream()
                .filter(remoteBranch -> remoteBranch.getBranchName().equals(remoteBranchName))
                .findAny()
                .orElse(null);
    }

    private boolean isBranchExistInLocal(Branch i_Branch)
    {
        String expectedRemoteBranchName = m_CurrentLocalRepository.getRemoteRepoRef().getName()
                + ResourceUtils.Slash + i_Branch.getBranchName();

        return m_CurrentLocalRepository.getRemoteBranches()
                .stream()
                .anyMatch(remoteBranch ->
                        remoteBranch.getBranchName().equals(expectedRemoteBranchName));
    }

    public LocalRepository getCurrentLocalRepository()
    {
        return m_CurrentLocalRepository;
    }
}
