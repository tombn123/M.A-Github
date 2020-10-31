package MAGit;

import MAGit.Constants.Constants;
import Objects.Blob;
import Objects.Commit;
import Objects.Folder;
import Objects.Item;
import Objects.branch.Branch;
import System.Engine;
import System.FolderDifferences;
import System.Repository;
import System.Users.User;
import XmlObjects.XMLMain;
import XmlObjects.repositoryWriters.RepositoryWriter;
import collaboration.LocalRepository;
import collaboration.Push;
import collaboration.RemoteBranch;
import common.MagitFileUtils;
import common.constants.ResourceUtils;
import common.constants.StringConstants;
import github.PullRequestLogic;
import github.commit.CommitData;
import github.notifications.ForkNotification;
import github.notifications.PullRequestNotification;
import github.notifications.Status;
import github.repository.RepositoryData;
import github.users.UserManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static common.constants.ResourceUtils.MainRepositoriesPath;

public class EngineAdapter
{
    //    private Engine engine = new Engine();
    private XMLMain xmlMain = new XMLMain();

    public void createUserFolder(User usernameFromParameter)
    {
        usernameFromParameter.getUserEngine().createUserFolder(usernameFromParameter.getUserName());
    }

    public Repository readRepositoryFromXMLFile(String xmlFileContent, String currentUserName) throws Exception
    {
        xmlMain.CheckXMLFile(xmlFileContent);
        return xmlMain.ParseAndWriteXML(xmlMain.getXmlRepository(), currentUserName);
    }

    public void createMainFolder() throws Exception
    {
        MagitFileUtils.CreateDirectory(MainRepositoriesPath);

    }

    public List<RepositoryData> buildAllUsersRepositoriesData(User i_UserToBuildRepositoryFor, boolean forClone) throws Exception
    {
        List<RepositoryData> allRepositoriesData = new ArrayList<>();
        File[] repositoriesFolders = MagitFileUtils.GetFilesInLocation(i_UserToBuildRepositoryFor.buildUserPath());

        for (File repositoryFolder : repositoriesFolders)
        {
            i_UserToBuildRepositoryFor.getUserEngine().PullAnExistingRepository(repositoryFolder.getPath());
            if (forClone)
                initListRepositoryData(i_UserToBuildRepositoryFor, allRepositoriesData, forClone);
            else
                initListRepositoryData(i_UserToBuildRepositoryFor, allRepositoriesData);
        }

        return allRepositoriesData;
    }

    private void initListRepositoryData(User i_UserToBuildRepositoryFor, List<RepositoryData> allRepositoriesData, boolean forClone)
    {
        if (!i_UserToBuildRepositoryFor.getUserEngine().IsLocalRepository())
        {
            Repository newRepo = i_UserToBuildRepositoryFor.getUserEngine().getCurrentRepository();
            RepositoryData repositoryData = new RepositoryData(newRepo.getName(),
                    newRepo.getActiveBranch().getPointedCommit().getSHA1(),
                    newRepo.getActiveBranch().getPointedCommit().getCommitMessage(),
                    newRepo.getActiveBranch().getBranchName(),
                    Integer.toString(newRepo.getAllCommitsSHA1ToCommit().size()),
                    i_UserToBuildRepositoryFor.getUserName());

            allRepositoriesData.add(repositoryData);
        }
    }

    private void initListRepositoryData(User i_UserToBuildRepositoryFor, List<RepositoryData> allRepositoriesData)
    {
        Repository newRepo = i_UserToBuildRepositoryFor.getUserEngine().getCurrentRepository();
        RepositoryData repositoryData = new RepositoryData(newRepo.getName(),
                newRepo.getActiveBranch().getPointedCommit().getSHA1(),
                newRepo.getActiveBranch().getPointedCommit().getCommitMessage(),
                newRepo.getActiveBranch().getBranchName(),
                Integer.toString(newRepo.getAllCommitsSHA1ToCommit().size()),
                i_UserToBuildRepositoryFor.getUserName());

        allRepositoriesData.add(repositoryData);
    }

    public void initRepositoryInSystemByName(String repositoryNameClicked, User loggedInUser) throws Exception
    {

        Repository repository = loggedInUser.getUserEngine().getNameToRepository().get(repositoryNameClicked);

        if (repository.getClass().equals(Repository.class))
            loggedInUser.getUserEngine().setCurrentRepository(repository);
        else
            loggedInUser.getUserEngine().setCurrentLocalRepository((LocalRepository) repository);

        /*String pathToUserFolderRepositories = loggedInUser.buildUserPath();

        File[] usersRepositories = MagitFileUtils.GetFilesInLocation(pathToUserFolderRepositories);

        for (File file : usersRepositories) {
            if (file.getName().equals(repositoryNameClicked))
                loggedInUser.getUserEngine().PullAnExistingRepository(file.getAbsolutePath());
        }*/
    }

    public void Clone(User i_UserNamerToCopyTo, User i_UserNameToCopyFrom, String i_RepositoryName, String i_RepositoryNewName) throws Exception
    {
        File dirToCloneFrom = Paths.get(ResourceUtils.MainRepositoriesPath + "\\" + i_UserNameToCopyFrom.getUserName() + "\\" + i_RepositoryName).toFile();
        File dirToCloneTo = Paths.get(ResourceUtils.MainRepositoriesPath + "\\" + i_UserNamerToCopyTo.getUserName() + "\\" + i_RepositoryNewName).toFile();
        i_UserNamerToCopyTo.getUserEngine().Clone(dirToCloneTo, i_RepositoryNewName, dirToCloneFrom);

        i_UserNameToCopyFrom.addNotification(new ForkNotification(new Date(), i_RepositoryName, i_UserNamerToCopyTo.getUserName()));
    }

    public List<Object> getBranchesList(User loggedInUser)
    {
        Engine engine = loggedInUser.getUserEngine();

        List<Branch> branches = new ArrayList<>();
        //branches.add(engine.getCurrentRepository().getActiveBranch());
        branches.addAll(engine.getCurrentRepository().getAllBranches());

        int index = branches.indexOf(engine.getCurrentRepository().getActiveBranch());
        branches.remove(index);
        branches.add(0, engine.getCurrentRepository().getActiveBranch());

        List<Object> branchesList = new ArrayList<>(branches);

        return branchesList;
    }

    public List<Object> getCommitsData(User loggedInUser)
    {
        Engine engine = loggedInUser.getUserEngine();

        List<Commit> commitList = new ArrayList<>();

        engine.getCurrentRepository().getActiveBranch().getAllCommitsPointed(commitList);

        return commitList.stream().map(commit ->
        {
            StringBuilder branchesPointedNames = new StringBuilder();
            List<String> branchesPointed = engine.getCurrentRepository().getBranchPointed(commit);

            return (Object) new CommitData(commit.getSHA1(), commit.getCommitMessage(),
                    commit.getUserCreated().getUserName(), String.join(", ", branchesPointed));

        }).collect(Collectors.toList());
    }

    public List<Object> getRepositoryName(User loggedInUser)
    {
        Engine engine = loggedInUser.getUserEngine();

        List<Object> lstToReturn = new ArrayList<>();
        lstToReturn.add(engine.getCurrentRepository().getName());
        lstToReturn.add(loggedInUser.getUserName());

        return lstToReturn;
    }

    public Set<String> GetBeenConnectedUserNameSet()
    {
        Set<String> userNamesSet = new HashSet<>();
        File[] allDirectories = Paths.get(ResourceUtils.MainRepositoriesPath).toFile().listFiles();
        for (int i = 0; i < allDirectories.length; i++)
        {
            userNamesSet.add(allDirectories[i].getName());
        }
        return userNamesSet;
    }

    public List<Object> getPullRequests(User loggedInUser)
    {
        List<Object> lstToReturn = new ArrayList<>();
        //lstToReturn.add(new PullRequestNotification());
        return lstToReturn;
    }

    public List<Object> isLocalRepository(User loggedInUser)
    {
        Engine engine = loggedInUser.getUserEngine();

        List<Object> isLocalList = new ArrayList<>();
        String isLocalRepository = engine.IsLocalRepository() ? StringConstants.YES : StringConstants.NO;

        isLocalList.add((Object) isLocalRepository);

        return isLocalList;
    }

    public void checkout(String branchName, User loggedInUser) throws Exception
    {
        loggedInUser.getUserEngine().CheckOut(branchName);
    }

    public void createNewLocalBranch(String branchName, String sha1Commit, User loggedInUser) throws Exception
    {
        loggedInUser.getUserEngine().CreateNewBranchToSystem(branchName, sha1Commit);
    }

    public String createNewRTB(String remoteBranchName, User loggedInUser) throws IOException, ParseException
    {
        Engine engine = loggedInUser.getUserEngine();

        LocalRepository localRepository = (LocalRepository) engine.getCurrentRepository();

        RemoteBranch remoteBranch = localRepository.findRemoteBranchByPredicate(remoteBranch1 -> remoteBranch1.getBranchName().equals(remoteBranchName));
        Commit pointedCommit = remoteBranch.getPointedCommit();

        String rtbName = remoteBranchName.split(ResourceUtils.Slash)[1];
        engine.CreateRTB(pointedCommit, rtbName);

        return rtbName;
    }

    public List<Object> getLocalBrances(User loggedInUser)
    {
        LocalRepository localRepository = (LocalRepository) loggedInUser.getUserEngine().getCurrentRepository();

        return localRepository.getLocalBranches().stream().
                map(branch -> (Object) branch).collect(Collectors.toList());
    }

    public void pushBranch(String branchToPushName, User loggedInUser, UserManager userManager) throws Exception
    {
        loggedInUser.getUserEngine().pushBranch(branchToPushName, userManager);
    }

    public void commitChanges(String commitMessage, User loggedInUser) throws Exception
    {
        loggedInUser.getUserEngine().CommitInCurrentRepository(commitMessage, null, loggedInUser);
    }

    public void pull(User loggedInUser) throws Exception
    {
        loggedInUser.getUserEngine().Pull();
    }

    public void push(User loggedInUser) throws Exception
    {
        Engine engine = loggedInUser.getUserEngine();

        Push pusher = new Push(engine, (LocalRepository) engine.getCurrentRepository());

        if (pusher.isPossibleToPush(loggedInUser))
            pusher.Push();
    }

    public void sendPullRequest(User loggedInUser, User userToNotify, String message, String branchBaseName, String branchTargetName, String remoteRepoName)
    {
        int pullRequestID = userToNotify.getPullRequestLogicList().size() + 1;
        PullRequestNotification pullRequestNotification = new PullRequestNotification(
                new Date(), remoteRepoName, Status.WAITING, userToNotify.getUserName(),
                branchTargetName, branchBaseName, message, pullRequestID);

        userToNotify.getPullRequestLogicList().add(new PullRequestLogic((pullRequestNotification), pullRequestID, userToNotify, loggedInUser));
        userToNotify.getNotificationList().add(pullRequestNotification);
    }

    public ItemInfo getItemInfoByPath(Path i_PathOfFile, User loggedInUser) throws Exception
    {
        Folder wc = loggedInUser.getUserEngine().getCurrentRepository().GetUpdatedWorkingCopy(loggedInUser);
        Item item = wc.getAllItemsMap().get(i_PathOfFile);
        return getItemInfo(item, loggedInUser);
    }

    public void ChangeFileInWorkingCopy(Path i_filePathToUpdate, String newContentOfFile) throws IOException
    {
        FileUtils.writeStringToFile(i_filePathToUpdate.toFile(), newContentOfFile, "UTF-8");
    }

    public ItemInfo GetWorkingCopyItemInfoByCommit(User i_LoggedInUser, String i_CommitSha1) throws Exception
    {
        Commit commitToGetInfoFrom = Commit.CreateCommitFromSha1(i_CommitSha1, i_LoggedInUser.getUserEngine().getCurrentRepository().GetObjectsFolderPath());
        return getItemInfo(commitToGetInfoFrom.getRootFolder(), i_LoggedInUser);

    }

    public ItemInfo getItemInfoByPathAndCommit(Path i_ItemPath, User i_LoggedInUser, String i_CommitSha1) throws Exception
    {
        Commit commitToGetInfoFrom = Commit.CreateCommitFromSha1(i_CommitSha1, i_LoggedInUser.getUserEngine().getCurrentRepository().GetObjectsFolderPath());
        Item item = commitToGetInfoFrom.getRootFolder().getAllItemsMap().get(i_ItemPath);
        return getItemInfo(item, i_LoggedInUser);
    }

    public void CreateNewFileInPath(Path i_PathOfDirectoryToPutFileIn, String newContentOfFile, String newFileName) throws IOException
    {
        File newFile = new File(i_PathOfDirectoryToPutFileIn.toString() + "\\" + newFileName + ".txt");
        newFile.createNewFile();
        FileUtils.writeStringToFile(newFile, newContentOfFile, "UTF-8");
    }

    public void RemoveFileFromWorkingCopy(Path i_FilePath, User i_user) throws Exception
    {
        i_FilePath.toFile().delete();
    }

    public String isDirtyWc(User i_loggedInUser) throws Exception
    {
        Folder wc = i_loggedInUser.getUserEngine().getCurrentRepository().GetUpdatedWorkingCopy(i_loggedInUser);
        Folder lastCommitWc = i_loggedInUser.getUserEngine().getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder();

        if (!wc.getSHA1().equals(lastCommitWc.getSHA1()))
            return "true";
        else
            return "false";
    }

    public ItemInfo GetWorkingCopyItemInfo(User i_user) throws Exception
    {
        Folder wc = i_user.getUserEngine().getCurrentRepository().GetUpdatedWorkingCopy(i_user);
        return getItemInfo(wc, i_user);
    }

    public ItemInfo getItemInfo(Item i_item, User i_user)
    {
        ItemInfo itemInfoResult = null;
        String itemName = i_item.getName();
        String itemPath = i_item.GetPath().toString();
        String itemSha1 = i_item.getSHA1();
        Item parentFolder = getParent(i_item, i_user);

        List<ItemInfo> itemInfos = new ArrayList<>();

        if (i_item.getTypeOfFile().equals(Item.TypeOfFile.FOLDER))
        {
            Folder folder = (Folder) i_item;
            List<Item> itemsList = folder.getListOfItems();
            itemsList.forEach(itemInItemList ->
            {
                if (itemInItemList.getTypeOfFile().equals(Item.TypeOfFile.BLOB))
                {
                    String fileContent = ((Blob) itemInItemList).getContent();
                    itemInfos.add(new ItemInfo(itemInItemList.getName(), Constants.FILE_TYPE, itemInItemList.getSHA1(), null, fileContent, folder.getSHA1(), itemInItemList.GetPath().toString(), folder.GetPath().toString()));

                } else
                {
                    itemInfos.add(new ItemInfo(itemInItemList.getName(), Constants.FOLDER_TYPE, itemInItemList.getSHA1(), null, null, folder.getSHA1(), itemInItemList.GetPath().toString(), folder.GetPath().toString()));
                }
            });
            ItemInfo[] items = new ItemInfo[itemInfos.size()];
            itemInfos.toArray(items);
            itemInfoResult = new ItemInfo(itemName, Constants.FOLDER_TYPE, itemSha1, items, null, parentFolder.getSHA1(), i_item.GetPath().toString(), parentFolder.GetPath().toString());
        } else
        {// it is a file
            itemInfoResult = new ItemInfo(itemName, Constants.FILE_TYPE, itemSha1, null, ((Blob) i_item).getContent(), parentFolder.getSHA1(), itemPath, parentFolder.GetPath().toString());
        }

        return itemInfoResult;
    }

    private Item getParent(Item i_item, User i_user)
    {
        if (!isRootFolder(i_item, i_user))
        {
            Path parentPath = i_item.GetPath().getParent();
            Map<Path, Item> allItemsMap = i_user.getUserEngine().getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder().getAllItemsMap();
            return allItemsMap.get(parentPath);
        } else return i_item;
    }

    private boolean isRootFolder(Item i_item, User i_user)
    {
        if (i_item.GetPath().equals(i_user.getUserEngine().getCurrentRepository().getActiveBranch().getPointedCommit().getRootFolder().GetPath()))
        {
            return true;
        } else return false;
    }

    public FolderDifferences GetChangesFromPullRequestLogic(PullRequestLogic i_Pullrequest, User i_user) throws Exception
    {

        //1. get the repository
        Map<String, Repository> repoMap = i_user.getUserEngine().getNameToRepository();
        Repository repositoryRequested = repoMap.get(i_Pullrequest.getNotification().getRepositoryName());

        //2. get the requested Branches to merge
        Branch baseBranch = repositoryRequested.getBranchByName(i_Pullrequest.getNotification().GetBaseBranchName());
        Branch targetBranch = repositoryRequested.getBranchByName(i_Pullrequest.getNotification().GetTargetBranchName());

        //3. get the differences in those branches
        FolderDifferences allDifferncesBetweenThePointedCommits = new FolderDifferences();
        Commit CommitIterator = targetBranch.getPointedCommit();

        while (!CommitIterator.getSHA1().equals(baseBranch.getPointedCommit().getSHA1()) || CommitIterator == null || CommitIterator.equals("null"))
        {
            //1. get difference between two
            FolderDifferences rootFolderDifference = Folder.FinedDifferences(CommitIterator.getRootFolder(), CommitIterator.GetPrevCommit().getRootFolder());
            //2. sum in the difference
            allDifferncesBetweenThePointedCommits.SumInFolderDiffernce(rootFolderDifference);
            //3. advance iterator
            CommitIterator = CommitIterator.GetPrevCommit();
        }
        if (CommitIterator == null || CommitIterator.equals("null"))
            throw new Exception("couldnt find the Commit in base branch, the base branch is:" + baseBranch.getBranchName());

        return allDifferncesBetweenThePointedCommits;


    }

    public PullRequestLogic getPullRequestInstance(User loggedInUser, int id)
    {
        return loggedInUser.getPullRequestLogicList()
                .stream()
                .filter(pullRequest -> pullRequest.getId() == id)
                .findAny().orElse(null);
    }

    public void fastForwardBaseToTarget(PullRequestLogic pullRequest, User loggedInUser) throws IOException, ParseException
    {
        Repository prRepository = loggedInUser.getUserEngine().getNameToRepository().get(pullRequest.getNotification().getRepositoryName());
        RepositoryWriter writer = new RepositoryWriter(prRepository);


        Branch baseBranch = prRepository.getBranchByName(pullRequest.getNotification().getBaseBranchName());
        Branch targetBranch = prRepository.getBranchByName(pullRequest.getNotification().getTargetBranchName());

        baseBranch.setPointedCommit(targetBranch.getPointedCommit());
        writer.WriteBranch(baseBranch);
    }

    class ItemInfo
    {
        String m_ItemName = null;
        String m_ItemPath = null;
        String m_ItemType = null;
        String m_ItemSha1 = null;
        ItemInfo[] m_ItemInfos = null;
        String m_FileContent = null;
        String m_ParentFolderSha1 = null;
        String m_ParentFolderPath = null;

        ItemInfo(String i_ItemName, String i_ItemType, String i_Sha1, ItemInfo[] i_ItemInfos, String i_FileContent, String i_ParentSha1, String i_ItemPath, String i_ParentPath)
        {
            m_ItemName = i_ItemName;
            m_ItemType = i_ItemType;
            m_ItemSha1 = i_Sha1;
            m_ItemInfos = i_ItemInfos;
            m_FileContent = i_FileContent;
            m_ParentFolderSha1 = i_ParentSha1;
            m_ParentFolderPath = i_ParentPath;
            m_ItemPath = i_ItemPath;
        }

    }

}
