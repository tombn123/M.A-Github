package System;

import Objects.Blob;
import Objects.Commit;
import Objects.Folder;
import Objects.branch.Branch;
import Objects.branch.BranchFactory;
import Objects.branch.BranchUtils;
import System.Users.User;
import XmlObjects.MagitRepository;
import XmlObjects.XMLMain;
import XmlObjects.repositoryWriters.LocalRepositoryWriter;
import XmlObjects.repositoryWriters.RepositoryWriter;
import collaboration.*;
import common.Enums;
import common.MagitFileUtils;
import common.constants.NumConstants;
import common.constants.ResourceUtils;
import common.constants.StringConstants;
import github.users.UserManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

import static common.constants.ResourceUtils.MainRepositoriesPath;

public class Engine
{
    public static final String sf_NOTHING_TO_COMMIT_ON = "There Is Nothing To Commit On";
    private Repository m_CurrentRepository = null;
    private XMLMain m_XMLMain;
    private LocalRepository m_CurrentLocalRepository = null;

    private Map<String, Repository> nameToRepository;

    public Engine()
    {
        m_XMLMain = new XMLMain();
        nameToRepository = new HashMap<String, Repository>();
    }

    public static void CreateRepositoryDirectories(Path i_rootFolderPath)
    {
        Path objectsFolderPath, branchesFolderPath, tempFolderPath;
        Path magitFolderPath = Paths.get(i_rootFolderPath.toString() + "\\.magit");
        magitFolderPath.toFile().mkdir();

        objectsFolderPath = Paths.get(magitFolderPath.toString() + "\\Objects");
        objectsFolderPath.toFile().mkdir();

        branchesFolderPath = Paths.get(magitFolderPath.toString() + "\\Branches");
        branchesFolderPath.toFile().mkdir();

        tempFolderPath = Paths.get(magitFolderPath.toString() + "\\Temp");
        tempFolderPath.toFile().mkdir();
    }

    public static void initNewPaths(Path i_NewPathOfRepository, Collection<Commit> allCommits)
    {
        for (Commit currentCommit : allCommits)
        {
            currentCommit.getRootFolder().initFolderPaths(i_NewPathOfRepository);
        }
    }

   /* public void UpdateNewUserInSystem(String i_UserName)
    {
        if (m_User == null)
        {
            m_User = new User(i_UserName);
        } else
        {
            m_User.setUserName(i_UserName);
        }
    }*/

    public void CommitInCurrentRepository(String i_CommitMessage, Commit prevSecondCommit, User loggedInUser) throws Exception
    {
        getCurrentRepository().CreateNewCommitAndUpdateActiveBranch(loggedInUser, i_CommitMessage, prevSecondCommit);
    }

    public void CreateNewRepository(Path i_PathToRootFolderOfRepository, String i_RepositoryName) throws Exception
    {
        Boolean exists = false;
        Path magitFolderPath = Paths.get(i_PathToRootFolderOfRepository.toString() + "\\" + ".magit");
        //check if the repository already exists
        if (magitFolderPath.toFile().exists())
        {
            String existsMessage = "The Repository in the path you gave already exists" + System.lineSeparator();
            throw new Exception(existsMessage);
        } else// making this folder a repository
        {
            createMagitFolderInRootFolder(i_PathToRootFolderOfRepository);
            Branch MasterBranch = new Branch("Master", null);
            m_CurrentRepository = new Repository(i_PathToRootFolderOfRepository, i_RepositoryName, MasterBranch);

            m_CurrentLocalRepository = null;
        }

    }

    //todo --> move this function to repository writer


    private void createMagitFolderInRootFolder(Path i_rootFolderPath) throws IOException
    {
        Path branchesFolderPath = Paths.get(i_rootFolderPath.toString() + Repository.sf_PathForBranches);
        CreateRepositoryDirectories(i_rootFolderPath);
        createHEADBranchAndAddressToMaster(branchesFolderPath);
        createEmptyMasterBranch(branchesFolderPath);
    }

    // this method creates the Master.txt branch in Branches folder
    private Path createEmptyMasterBranch(Path i_BranchFolderPath) throws IOException
    {
        File masterBranch = new File(i_BranchFolderPath.toString() + "\\Master.txt");
        masterBranch.createNewFile();
        return masterBranch.toPath();
    }

    private void createHEADBranchAndAddressToMaster(Path i_BranchFolderPath) throws IOException
    {
        Path HEADFilePath = Paths.get(i_BranchFolderPath.toString() + "\\HEAD.txt");
        Files.createFile(HEADFilePath);
        org.apache.commons.io.FileUtils.writeStringToFile(HEADFilePath.toFile(), "Master", "UTF-8", false);
    }

    public FolderDifferences ShowStatus(User loggedInUser) throws Exception
    {
        //1. create the currentWorkingCopy as Folder
        FolderDifferences differences = null;
        Folder wc = this.getCurrentRepository().GetUpdatedWorkingCopy(loggedInUser);
        if (this.getCurrentRepository().getActiveBranch().getPointedCommit() == null)
            throw new Exception("Before show status, you need to commit first!");

        Folder lastCommitWc = this.getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder();

        if (!wc.getSHA1().equals(lastCommitWc.getSHA1()))
            differences = Folder.FinedDifferences(wc, lastCommitWc);


        return differences;
    }

    public void CheckOut(String i_BranchName) throws Exception
    {
        //1. set active branch to selectd branch
        replaceActiveBranch(i_BranchName);

        removeFilesFromWCAndSpanNewCommitInActiveBranch();
    }

    private void replaceActiveBranch(String i_branchName) throws Exception
    {
        //3. insert to HEAD.txt (in Branches folder) the name of the branch
        File HEADFile = Paths.get(getCurrentRepository().getBranchesFolderPath().toString() + ResourceUtils.Slash + ResourceUtils.HEAD).toFile();
        FileUtils.writeStringToFile(HEADFile, i_branchName, "UTF-8", false);

        Branch newActiveBranch = getCurrentRepository().getActiveBranches().stream().filter(branch -> branch.getBranchName().equals(i_branchName))
                .findAny().orElse(null);

        getCurrentRepository().setActiveBranch(newActiveBranch);
    }

    private void removeFilesFromWCAndSpanNewCommitInActiveBranch() throws IOException
    {
        //2. remove previous  files and folders
        Folder.RemoveFilesAndFoldersWithoutMagit(this.getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder().GetPath());
        //3. span rootFolder in WC
        Folder.SpanDirectory(this.getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder());
    }

    public void GetCommitHistoryInActiveBranch() throws IOException
    {
        Branch activeBranch = this.m_CurrentRepository.getActiveBranch();
        String commitHistoryOfActiveBranch = Branch.GetCommitHistory(activeBranch, m_CurrentRepository.GetObjectsFolderPath());
        System.out.println(commitHistoryOfActiveBranch);
    }

    public Repository getCurrentRepository()
    {
        return m_CurrentRepository == null ? m_CurrentLocalRepository : m_CurrentRepository;
    }

    public void setCurrentRepository(Repository m_CurrentRepository)
    {
        this.m_CurrentRepository = m_CurrentRepository;
    }

    public void PullAnExistingRepository(String repositoryPathAsString, String repositoryName) throws Exception
    {
        Path repositoryPath = Paths.get(repositoryPathAsString);

        loadExistingRepositoryFromFileSystem(repositoryPathAsString, repositoryPath, repositoryName);
    }

    public void PullAnExistingRepository(String i_repositoryPathAsString) throws Exception
    {
        Path repositoryPath = Paths.get(i_repositoryPathAsString);

        String i_NameOfRepository = GetExistingRepositoryName(repositoryPath.toFile());

        loadExistingRepositoryFromFileSystem(i_repositoryPathAsString, repositoryPath, i_NameOfRepository);
    }

    private void loadExistingRepositoryFromFileSystem(String i_repositoryPathAsString, Path repositoryPath, String i_NameOfRepository) throws Exception
    {
        Optional<Branch> activeBranch;
        Repository repository;
        if (!repositoryPath.toFile().exists())
        {
            throw new FileNotFoundException(repositoryPath.toString() + " does not exist - please make sure you are giving a correct path");
        }
        Path branchFolderPath = Paths.get(repositoryPath.toString() + "\\.magit\\Branches");

        Path HEAD = Paths.get(branchFolderPath.toString() + "\\HEAD.txt");
        String activeBranchName = MagitFileUtils.GetContentFile(HEAD.toFile());


        if (MagitFileUtils.IsFolderExist(branchFolderPath))// folder of remote branches ---> that means this is local repository
            createLocalRepository(i_repositoryPathAsString, activeBranchName, i_NameOfRepository);
        else
        {
            Map<String, Commit> allCommitsInRepositoryMap = createMapOfCommits(Paths.get(i_repositoryPathAsString + ResourceUtils.AdditinalPathObjects));
            List<Branch> allBranches = Branch.GetAllBranches(branchFolderPath, allCommitsInRepositoryMap);
            activeBranch = Branch.GetHeadBranch(allBranches, activeBranchName);

            repository = new Repository(activeBranch.get(), repositoryPath, i_NameOfRepository, allBranches, allCommitsInRepositoryMap);
            this.m_CurrentRepository = repository;
            m_CurrentLocalRepository = null;
        }
    }

    private void createLocalRepository(String repositoryPath, String activeBranchName, String nameOfRepository) throws Exception
    {
        m_CurrentRepository = null;

        List<Branch> branches = new ArrayList<>();
        List<RemoteBranch> remoteBranches = new ArrayList<>();
        List<RemoteTrackingBranch> remoteTrackingBranches = new ArrayList<>();

        Path pathToObjects = Paths.get(repositoryPath + ResourceUtils.AdditinalPathObjects);

        Map<String, Commit> allCommits = createMapOfCommits(pathToObjects);

        RemoteRepositoryRef remoteRepositoryRef = RemoteRepositoryRef.CreateRepositoryRefFromFile(repositoryPath);

        createAllBranches(repositoryPath, branches, remoteBranches, remoteRepositoryRef.getName(), remoteTrackingBranches, allCommits);

        m_CurrentLocalRepository = new LocalRepository(null, Paths.get(repositoryPath), nameOfRepository, branches, allCommits,
                remoteTrackingBranches, remoteBranches, remoteRepositoryRef);

        m_CurrentLocalRepository.setActiveBranch(m_CurrentLocalRepository.FindBranchInActiveBranchesByName(activeBranchName));
    }

    private void createAllBranches(String repositoryPath, List<Branch> branches, List<RemoteBranch> remoteBranches, String remoteRepoName,
                                   List<RemoteTrackingBranch> remoteTrackingBranches, Map<String, Commit> allCommits) throws IOException
    {
        String branchesPath = repositoryPath + ResourceUtils.AdditinalPathBranches;

        File[] branchesFiles = MagitFileUtils.GetFilesInLocation(branchesPath);

        //create remote tracking branches and ordinary branches
        for (File branchFile : branchesFiles)
        {
            if (!branchFile.getName().equals(ResourceUtils.HEAD) && (!branchFile.isDirectory()))
            {
                Enums.BranchType type;
                String branchName = MagitFileUtils.RemoveExtension(branchFile.toPath());
                Commit branchCommit = allCommits.get(MagitFileUtils.GetTextLines(Paths.get(branchFile.getAbsolutePath())).get(0));

                if (MagitFileUtils.IsRemoteTrackingBranch(branchFile))
                    type = Enums.BranchType.REMOTE_TRACKING_BRANCH;
                else
                    type = Enums.BranchType.BRANCH;

                BranchFactory.CreateBranchInBranchFactory(branches, remoteTrackingBranches, remoteBranches, branchName, branchCommit,
                        type);
            }
        }

        //create remote branches

        String remoteBranchesPath = branchesPath + ResourceUtils.Slash + remoteRepoName;
        File[] remoteBranchesFiles = MagitFileUtils.GetFilesInLocation(remoteBranchesPath);

        for (File remoteBranchFile : remoteBranchesFiles)
        {
            String branchName = RemoteBranch.GetRemoteBranchName(MagitFileUtils.RemoveExtension(remoteBranchFile.toPath()), remoteRepoName);
            Commit branchCommit = allCommits.get(MagitFileUtils.GetTextLines(Paths.get(remoteBranchFile.getAbsolutePath())).get(0));

            BranchFactory.CreateBranchInBranchFactory(branches, remoteTrackingBranches, remoteBranches, branchName, branchCommit,
                    Enums.BranchType.REMOTE_BRANCH);
        }
    }

    private Map<String, Commit> createMapOfCommits(Path i_ObjectsFolder) throws Exception
    {
        Map<String, Commit> resMap = new HashMap<>();
        File[] allObjects = i_ObjectsFolder.toFile().listFiles();
        for (int i = 0; i < allObjects.length; i++)
        {
            File currObject = allObjects[i];
            if (Commit.IsSha1ValidForCommit(currObject.getName(), i_ObjectsFolder))
            {
                String commitsSha1 = currObject.getName();
                Commit commitFromFile = Commit.CreateCommitFromSha1(commitsSha1, i_ObjectsFolder);

                addCommitAndAllItsPrevsToMap(commitFromFile, resMap);
            }
        }
        return resMap;
    }

    private void addCommitAndAllItsPrevsToMap(Commit commitFromFile, Map<String, Commit> resMap)
    {
        if (isInCommitMap(commitFromFile, resMap))
            return;

        resMap.put(commitFromFile.getSHA1(), commitFromFile);

        if (commitFromFile.ThereIsPrevCommit(NumConstants.ONE))
            if (!isInCommitMap(commitFromFile.GetPrevCommit(), resMap))
                addCommitAndAllItsPrevsToMap(commitFromFile.GetPrevCommit(), resMap);
            else//if there is prev commit and it is on map concat them
                commitFromFile.setPrevCommit(resMap.get(commitFromFile.GetPrevCommit().getSHA1()));

        if (commitFromFile.ThereIsPrevCommit(NumConstants.TWO))
            if (!isInCommitMap(commitFromFile.GetSecondPrevCommit(), resMap))
                addCommitAndAllItsPrevsToMap(commitFromFile.GetSecondPrevCommit(), resMap);
            else//if there is prev commit and it is on map concat them
                commitFromFile.setSecondPrevCommit(resMap.get(commitFromFile.GetSecondPrevCommit().getSHA1()));
    }

   /* private void concatPrevCommit(Commit commitFromFile, Map<String, Commit> resMap, int prevNum)
    {
        if (commitFromFile.ThereIsPrevCommit(prevNum))
            if (!resMap.containsKey(commitFromFile.GetPrevCommit().getSHA1()))
                addCommitAndAllItsPrevsToMap(commitFromFile.GetPrevCommit(), resMap);
            else//if there is prev commit and it is on map concat them
                commitFromFile.setPrevCommit(resMap.get(commitFromFile.getSHA1()));
    }*/

    private boolean isInCommitMap(Commit commitFromFile, Map<String, Commit> resMap)
    {
        return resMap.containsKey(commitFromFile.getSHA1());
    }

    public String ShowAllCurrentCommitData()
    {
        return this.m_CurrentRepository.getActiveBranch().getPointedCommit().getAllFolderAndBlobsData();
    }

    public void CreateNewBranchToSystem(String i_NameOfNewBranch, String i_SHA1OfCommit) throws Exception
    {
        //check if commit exist
        checkIfSHA1CommitExist(i_SHA1OfCommit);
        //check if branch name already exist
        if (isBranchExist(i_NameOfNewBranch))
            throw new Exception("Error!" + System.lineSeparator() + "Branch name already exist." + System.lineSeparator());

        if (getCurrentRepository().getActiveBranch().getPointedCommit() != null)
        {
            getCurrentRepository().AddingNewBranchInRepository(i_NameOfNewBranch, i_SHA1OfCommit);
        } else
            throw new Exception("Error!" + System.lineSeparator() + "There are no commits yet" + System.lineSeparator());
    }

    private boolean isaCommitExistBySHA1(String i_SHA1OfCommit)
    {
        return getCurrentRepository().getAllCommitsSHA1ToCommit().containsKey(i_SHA1OfCommit);
    }

    private boolean isBranchExist(String i_NameOfNewBranch)
    {
        return getCurrentRepository().getAllBranches()
                .stream()
                .anyMatch(branch ->
                        branch.getBranchName().equals(i_NameOfNewBranch));
    }

    public void DeleteBranchFromSystem(String i_BranchNameToErase) throws Exception
    {
        if (i_BranchNameToErase.equals(getCurrentRepository().getActiveBranch().getBranchName()))
            throw new Exception("Error! Can not erase HEAD Branch");

        String pathBranch = getCurrentRepository().getRepositoryPath().toString()
                + Repository.sf_PathForBranches
                + Repository.sf_Slash
                + i_BranchNameToErase
                + Repository.sf_txtExtension;

        File tempFileForCheckingExistence = new File(pathBranch);

        if (!tempFileForCheckingExistence.exists())
            throw new Exception("Error! Branch doesnt exist!");
        else
            deleteBranch(tempFileForCheckingExistence, i_BranchNameToErase);
    }

    private void deleteBranch(File i_TempFileForCheckingExistence, String i_BranchNameToErase)
    {
        i_TempFileForCheckingExistence.delete();

        if (IsLocalRepository())
        {
            LocalRepository localRepository = (LocalRepository) getCurrentRepository();
            localRepository.getRemoteTrackingBranches().removeIf(branch -> branch.getBranchName().equals(i_BranchNameToErase));
            localRepository.getLocalBranches().removeIf(branch -> branch.getBranchName().equals(i_BranchNameToErase));

        } else
            getCurrentRepository().getAllBranches().removeIf(branch -> branch.getBranchName().equals(i_BranchNameToErase));
    }

    public void RemoveTempFolder() throws IOException
    {
        Path tempFolder = this.m_CurrentRepository.GetTempFolderPath();
        if (tempFolder.toFile().exists())
        {
            org.apache.commons.io.FileUtils.deleteDirectory(tempFolder.toFile());
        }
    }

   /* public void ExecuteUserChoice(int i_RepoChoice, MagitRepository i_MagitRepository,
                                  XMLMain i_XmlMain) throws Exception
    {
        switch (i_RepoChoice)
        {
            case 1:
                Folder.DeleteDirectory(i_MagitRepository.getLocation());
                m_CurrentRepository = i_XmlMain.ParseAndWriteXML(i_MagitRepository);
                WriteRepositoryNameFileInMagitRepository();
                AssignFitRepository(i_MagitRepository, i_XmlMain);
                break;

            case 2:
                PullAnExistingRepository(i_MagitRepository.getLocation(), i_MagitRepository.getName());
                break;
        }
    }*/

    /*public boolean CheckIfRootFolderChanged() throws Exception
    {
        //1. get sha1 of root folder of last commit
        Folder rootFolderOfCommit = this.getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder();
        //2. get working copy
        Folder wc = this.getCurrentRepository().GetUpdatedWorkingCopy(this.m_User);
        //3. compare both Sha1 - if equal then there are no changes
        if (wc.getSHA1().equals(rootFolderOfCommit.getSHA1()))
        {
            return false;
        } else
            return true;
    }*/

    public void AssignFitRepository(MagitRepository i_MagitRepository, XMLMain i_XmlMain)
    {
        if (i_XmlMain.IsLocalRepository(i_MagitRepository))
        {
            m_CurrentLocalRepository = (LocalRepository) m_CurrentRepository;
            m_CurrentRepository = null;
        }
    }

    public void CheckExistenceCurrentRepository() throws Exception
    {
        if (m_CurrentRepository == null)
            throw new Exception("Can't execute this operation!" + System.lineSeparator() +
                    "Repository doesn't exist in System");
    }

    private void checkExistenceOfCommit() throws Exception
    {
        if (getCurrentRepository().ThereAreNoCmmitsYet())
            throw new Exception("Can't execute this operation!" + System.lineSeparator() +
                    "There are no commits in systen yet");
    }

    public void CheckIfRepoAndCommitInSystem() throws Exception
    {
        CheckExistenceCurrentRepository();
        checkExistenceOfCommit();
    }

    public void ResetHeadBranch(String i_Sha1OfCommit) throws Exception
    {
        checkIfSHA1CommitExist(i_Sha1OfCommit);

        Commit commitRequested = getCurrentRepository().getAllCommitsSHA1ToCommit().get(i_Sha1OfCommit);

        String branchFilePath = getCurrentRepository().getBranchesFolderPath().toString()
                + Repository.sf_Slash
                + getCurrentRepository().getActiveBranch().getBranchName()
                + Repository.sf_txtExtension;

        String contentToWrite = MagitFileUtils.IsRemoteTrackingBranch(new File(branchFilePath)) ?
                commitRequested.getSHA1() + System.lineSeparator() + StringConstants.TRUE :
                commitRequested.getSHA1();

        MagitFileUtils.OverwriteContentInFile(contentToWrite, branchFilePath);

        getCurrentRepository().getActiveBranch().setPointedCommit(commitRequested);

        removeFilesFromWCAndSpanNewCommitInActiveBranch();
    }

    private void checkIfSHA1CommitExist(String i_Sha1OfCommit) throws Exception
    {
        if (!isaCommitExistBySHA1(i_Sha1OfCommit))
        {
            throw new Exception("Error!" + System.lineSeparator() + "Commit doesn't exist." + System.lineSeparator());
        }
    }

    public Map<String, Repository> getNameToRepository()
    {
        return nameToRepository;
    }

    public FolderDifferences ShowDeltaCommits(Commit i_Commit)
    {
        Commit prevCommit = null;
        if (i_Commit.ThereIsPrevCommit(NumConstants.FIRST))
            prevCommit = getCurrentRepository().getAllCommitsSHA1ToCommit().get(i_Commit.GetPrevCommit().getSHA1());
        else
            return null;

        return Folder.FinedDifferences(prevCommit.getRootFolder(), i_Commit.getRootFolder());
    }

    public void Clone(File i_DirCloneTo, String i_RepositoryName, File i_DirCloneFrom) throws Exception
    {
        List<RemoteBranch> remoteBranches = new ArrayList<>();
        List<RemoteTrackingBranch> remoteTrackingBranches = new ArrayList<>();

        PullAnExistingRepository(i_DirCloneFrom.getPath());

        createRemoteBranches(remoteBranches, i_DirCloneFrom.getName());

        RemoteTrackingBranch remoteHeadTrackingBranch = new RemoteTrackingBranch(m_CurrentRepository.getActiveBranch());

        initNewPaths(i_DirCloneTo.toPath(), m_CurrentRepository.getAllCommitsSHA1ToCommit().values());

        remoteTrackingBranches.add(remoteHeadTrackingBranch);

        m_CurrentLocalRepository = new LocalRepository(remoteHeadTrackingBranch, i_DirCloneTo.toPath(),
                i_RepositoryName, null, m_CurrentRepository.getAllCommitsSHA1ToCommit(),
                remoteTrackingBranches, remoteBranches, new RemoteRepositoryRef(i_DirCloneFrom.getName(),
                i_DirCloneFrom.toPath()));

        m_CurrentRepository = null;

        LocalRepositoryWriter localRepositoryWriter = new LocalRepositoryWriter(m_CurrentLocalRepository);
        localRepositoryWriter.WriteRepositoryToFileSystem(m_CurrentLocalRepository.getActiveBranch().getBranchName());
    }

    private void createRemoteBranches(List<RemoteBranch> i_RemoteBranches, String i_CloneFromRepoName)
    {
        m_CurrentRepository.getAllBranches().stream().forEach(branch ->
        {
            RemoteBranch remoteBranch = RemoteBranch.CreateRemoteBranchFromBranch(branch, i_CloneFromRepoName);
            i_RemoteBranches.add(remoteBranch);
        });
    }

    public void setCurrentLocalRepository(LocalRepository m_CurrentLocalRepository)
    {
        this.m_CurrentLocalRepository = m_CurrentLocalRepository;
    }

    public void Fetch() throws Exception
    {
        Fetch fetcher = new Fetch(this, m_CurrentLocalRepository);
        fetcher.FetchAllObjects();
    }

    /*public void reassignValuesOfRepositories(Fetch fetcher)
    {
        m_CurrentLocalRepository = fetcher.getCurrentLocalRepository();
        m_CurrentRepository = null;
    }*/

    public void Pull() throws Exception
    {
        Fetch fetcher = new Fetch(this, m_CurrentLocalRepository);

        Map<String, Commit> allCommitsInLocal = m_CurrentLocalRepository.getAllCommitsSHA1ToCommit();

        Branch activeBranchInRemote = fetcher.getRemoteRepositoryToFetchFrom().getActiveBranch();
        fetcher.FetchBranch(activeBranchInRemote);

        Branch activeBranchInLocal = m_CurrentLocalRepository.getActiveBranch();
        Commit headCommitInLocalAfterFetched = allCommitsInLocal.get
                (activeBranchInRemote.getPointedCommit().getSHA1());

        initNewPaths(m_CurrentLocalRepository.getRepositoryPath(), m_CurrentRepository.getAllCommitsSHA1ToCommit().values());
        activeBranchInLocal.setPointedCommit(headCommitInLocalAfterFetched);

        writeBranchInSystem(activeBranchInLocal);

        removeFilesFromWCAndSpanNewCommitInActiveBranch();
    }

    private void writeBranchInSystem(Branch activeBranchInLocal) throws IOException, ParseException
    {
        LocalRepositoryWriter writer = new LocalRepositoryWriter(m_CurrentLocalRepository);

        if (BranchUtils.IsRemoteTrackingBranch(activeBranchInLocal))
            writer.WriteRemoteTrackingBranch((RemoteTrackingBranch) activeBranchInLocal);
        else
            writer.WriteBranch(activeBranchInLocal);
    }

    public boolean IsLocalRepository()
    {
        return m_CurrentRepository == null ? true : false;
    }

    public boolean IsHeadBranch(String branchName)
    {
        return branchName.equals(getCurrentRepository().getActiveBranch().getBranchName());
    }

    public void CreateRTB(Commit commit, String branchName) throws IOException, ParseException
    {
        LocalRepository localRepository = (LocalRepository) getCurrentRepository();

        BranchFactory.CreateBranchInBranchFactory(localRepository.getLocalBranches(), localRepository.getRemoteTrackingBranches(),
                localRepository.getRemoteBranches(), branchName, commit, Enums.BranchType.REMOTE_TRACKING_BRANCH);

        localRepository.setActiveBranch(localRepository.findRemoteTrackingBranchByPredicate(branch -> branch.getBranchName().equals(branchName)));

        LocalRepositoryWriter writer = new LocalRepositoryWriter(localRepository);
        RemoteTrackingBranch remoteTrackingBranch = localRepository.findRemoteTrackingBranchByPredicate(RTB ->
                RTB.getBranchName().equals(branchName));

        writer.WriteAllRemoteTrackingBranches(branchName);
    }


    public void SetConflictsForMergeInRepository(String i_pushingBranchName) throws Exception
    {
        Branch pushingBranch = this.getCurrentRepository().getBranchByName(i_pushingBranchName);
        MergeConflictsAndMergedItems mergeConflictsAndMergedItems = getCurrentRepository().getActiveBranch().GetConflictsForMerge(pushingBranch, getCurrentRepository().getRepositoryPath(), createMapOfCommits(this.getCurrentRepository().GetObjectsFolderPath()));
        setMergeConflictsInstance(mergeConflictsAndMergedItems);

    }

    private void setMergeConflictsInstance(MergeConflictsAndMergedItems i_MergeConflictsAndMergedItems)
    {
        this.getCurrentRepository().SetMergeConflictsInstance(i_MergeConflictsAndMergedItems);
    }

    public MergeConflictsAndMergedItems GetConflictsForMerge()
    {
        return this.getCurrentRepository().getConflictsItemsAndNames();
    }

    public MergeConflictsAndMergedItems GetConflictsForMerge(String i_pushingBranchName) throws Exception
    {
        {
            Branch pushingBranch = this.getCurrentRepository().getBranchByName(i_pushingBranchName);
            return getCurrentRepository().getActiveBranch().GetConflictsForMerge(pushingBranch, getCurrentRepository().getRepositoryPath(),
                    getCurrentRepository().getAllCommitsSHA1ToCommit());
        }
    }

    public ConflictingItems getConflictingItemsByName(String conflictingItemName)
    {
        return getCurrentRepository().getConflictsItemsAndNames().getConflictingItemByName(conflictingItemName);
    }

    public void CreateChosenBlobInWC(String blobText, Blob chosenBlob) throws IOException
    {
        getCurrentRepository().getConflictsItemsAndNames().CreateChosenBlobInWC(blobText, chosenBlob);
    }

    /* public void CreateCommitMerge(String commitMessage, String selectedBranchName) throws Exception
     {
         Branch selectedBranch;

         if (IsLocalRepository())
         {
             LocalRepository localRepository = (LocalRepository) getCurrentRepository();
             selectedBranch = localRepository.FindBranchInActiveBranchesByName(selectedBranchName);
         } else
         {
             selectedBranch = getCurrentRepository().findBranchByPredicate(branch ->
                     branch.getBranchName().equals(selectedBranchName));
         }

         CommitInCurrentRepository(commitMessage, selectedBranch.getPointedCommit(), loggedInUser);
     }
 */
    public void FastForwardBranch(String selectedBranchName) throws IOException
    {
        Branch selectedBranch = getCurrentRepository().getActiveBranches().stream().filter(branch ->
                branch.getBranchName().equals(selectedBranchName)).findAny().orElse(null);

        getCurrentRepository().getActiveBranch().setPointedCommit(selectedBranch.getPointedCommit());

        Folder.RemoveFilesAndFoldersWithoutMagit(getCurrentRepository().getRepositoryPath());
        Folder.SpanDirectory(getCurrentRepository().m_ActiveBranch.getPointedCommit().getRootFolder());
    }

    public String GetExistingRepositoryName(File i_existingRepositoryFolder) throws IOException
    {
        String pathToMagit = i_existingRepositoryFolder.getAbsolutePath() + ResourceUtils.AdditinalPathMagit;

        for (File file :
                MagitFileUtils.GetFilesInLocation(pathToMagit))
        {

            if (file.getName().equals(ResourceUtils.RepoName + ResourceUtils.TxtExtension))
                return FileUtils.readFileToString(file, "UTF-8");
        }

        return null;
    }

    /* public void createMainRepositoryFolder() throws Exception
     {
         MagitFileUtils.CreateDirectory(MainRepositoriesPath);
     }
 */
    public void createUserFolder(String usernameFromParameter)
    {
        //MagitFileUtils.CreateWholePathDirecories(MainRepositoriesPath + ResourceUtils.Slash + usernameFromParameter);
        MagitFileUtils.CreateDirectory(MainRepositoriesPath + ResourceUtils.Slash + usernameFromParameter);
    }

    public void pushBranch(String branchToPushName, UserManager userManager) throws Exception
    {
        Fetch fetcher = new Fetch(this, m_CurrentLocalRepository);
//        Repository remoteRepository = fetcher.getRemoteRepositoryToFetchFrom();
        LocalRepositoryWriter localRepositoryWriter = new LocalRepositoryWriter(m_CurrentLocalRepository);

        /*-----------------find user of remote repo------------------*/

        String[] pathToNotifiy = m_CurrentLocalRepository.getRemoteRepoRef().getRepoPath().toString().split("\\\\");
        String userToNotifyInString = pathToNotifiy[pathToNotifiy.length - 2];

        User userOfRemoteRepo = userManager.getUserByName(userToNotifyInString);
        Repository remoteRepository = userOfRemoteRepo.getUserEngine().nameToRepository.get(m_CurrentLocalRepository.getRemoteRepoRef().getName());

        RepositoryWriter repositoryWriter = new RepositoryWriter(remoteRepository);
        /*-----------------push branch to RR------------------*/

        Branch branchToPush = getCurrentRepository().findBranchByPredicate(branch ->
                branch.getBranchName().equals(branchToPushName));

        repositoryWriter.WriteBranch(branchToPush);

        remoteRepository.getAllBranches().add(branchToPush);

        /*-----make local branch to rtb and create remote branch----*/

        RemoteBranch newRemoteBranchPushed = new RemoteBranch(remoteRepository.getName() + ResourceUtils.Slash + branchToPushName,
                branchToPush.getPointedCommit());
        RemoteTrackingBranch newRTBPushed = new RemoteTrackingBranch(branchToPush);

        m_CurrentLocalRepository.getLocalBranches().remove(branchToPush);
        m_CurrentLocalRepository.getRemoteTrackingBranches().add(
                newRTBPushed);

        m_CurrentLocalRepository.getRemoteBranches().add(
                newRemoteBranchPushed);

        localRepositoryWriter.WriteRemoteBranch(newRemoteBranchPushed);
        localRepositoryWriter.WriteRemoteTrackingBranch(newRTBPushed);
    }
}