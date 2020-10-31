package System;

import Objects.Blob;
import Objects.Commit;
import Objects.Folder;
import Objects.Item;
import Objects.branch.Branch;
import System.Users.User;
import common.MagitFileUtils;
import common.constants.StringConstants;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Repository
{
    //At first, we dont need initialized those fields
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static final String sf_PathForObjects = "\\.magit\\Objects";
    public static final String sf_PathForBranches = "\\.magit\\Branches";
    public static final String sf_PathForTempFolder = "\\.magit\\Temp";
    public static final String sf_Slash = "\\";
    public static final String sf_txtExtension = ".txt";
    protected Branch m_ActiveBranch;
    protected List<Branch> m_Branches = null;
    private MergeConflictsAndMergedItems m_ConflictsAndItems = null;
    private Path m_RepositoryPath;
    private String m_RepositoryName;
    private Path m_ObjectsFolderPath;
    private Path m_BranchesFolderPath;
    private Path m_TempFolderPath;
    private Map<String, Commit> m_AllCommitsSHA1ToCommit = new HashMap<String, Commit>();

    public Repository(Path i_RepositoryPath, String i_RepositoryName, Branch i_ActiveBranch)
    {
        this.m_RepositoryPath = i_RepositoryPath;
        this.m_RepositoryName = i_RepositoryName;
        m_ActiveBranch = i_ActiveBranch;

        if (m_Branches == null)
        {
            m_Branches = new ArrayList<Branch>();
        }

        //m_ActiveBranch = new Branch("Master", null);// once there is a first commit for than the current commit is updated
        m_Branches.add(i_ActiveBranch);
        settingAllPathsInRepository(i_RepositoryPath);

    }

    public Repository(Branch i_ActiveBranch, Path i_RepositoryPath, String i_RepositoryName, List<Branch> i_AllBranches,
                      Map<String, Commit> i_AllCommitsRepository)
    {
        this.m_AllCommitsSHA1ToCommit = i_AllCommitsRepository;
        this.m_ActiveBranch = i_ActiveBranch;
        this.m_RepositoryPath = i_RepositoryPath;
        this.m_RepositoryName = i_RepositoryName;
        this.m_Branches = i_AllBranches;

        setAllPathsOfRepositoryDirectories(i_RepositoryPath);

    }

    // this method creates a temp file - writes into it and returns its Path
    public static Path WritingStringInAFile(String i_FileContent, String i_FileName)
    {
        try
        {
            File temp = File.createTempFile(i_FileName, ".txt");
            temp.deleteOnExit();
            Files.write(temp.toPath(), i_FileContent.getBytes());
            return temp.toPath();
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public List<Branch> getActiveBranches()
    {
        return m_Branches;
    }

    public String getName()
    {
        return m_RepositoryName;
    }

    public void setBranches(List<Branch> m_Branches)
    {
        this.m_Branches = m_Branches;
    }

    public Branch findBranchByPredicate(Predicate<Branch> predicate)
    {
        if (m_Branches != null)
        {

            return m_Branches.stream().filter(branch ->
                    predicate.test(branch)).findAny().orElse(null);
        }

        return null;
    }

    public Map<String, Commit> getAllCommitsSHA1ToCommit()
    {
        return m_AllCommitsSHA1ToCommit;
    }

    private void settingAllPathsInRepository(Path i_RepositoryPath)
    {
        setAllPathsOfRepositoryDirectories(i_RepositoryPath);
    }

    private void setAllPathsOfRepositoryDirectories(Path i_RepositoryPath)
    {
        String ObjectsPath = (i_RepositoryPath.toString() + sf_PathForObjects);
        m_ObjectsFolderPath = Paths.get(ObjectsPath);
        String branchesPath = (i_RepositoryPath.toString() + sf_PathForBranches);
        m_BranchesFolderPath = Paths.get(branchesPath);
        String TempPath = (i_RepositoryPath.toString() + sf_PathForTempFolder);
        m_TempFolderPath = Paths.get(TempPath);
    }

    //this method is used when creating a new commit that hasnt been created before - aka "commit changes"
    public Commit createNewInstanceCommit(User i_User, Folder i_RootFolder, String i_CommitMessage, Commit prevSecondCommit) throws Exception
    {
        Date date = new Date();
        String prevCommitSha1 = null, prevSecondCommitSha1 = null;
        if (this.m_ActiveBranch.getPointedCommit() != null) // if its the first time commiting then there is no pointed commit yet
            prevCommitSha1 = this.m_ActiveBranch.getPointedCommit().getSHA1();

        if (prevSecondCommit != null)
            prevSecondCommitSha1 = prevSecondCommit.getSHA1();

        String CommitSha1 = Commit.createSha1ForCommit(i_RootFolder, prevCommitSha1, prevSecondCommitSha1, i_CommitMessage, i_User, date);
        Commit theNewCommit = new Commit(i_RootFolder, CommitSha1, this.m_ActiveBranch.getPointedCommit(), prevSecondCommit, i_CommitMessage, i_User, date);
        m_AllCommitsSHA1ToCommit.put(CommitSha1, theNewCommit);
        return theNewCommit;
    }

    public FolderDifferences CreateNewCommitAndUpdateActiveBranch(User i_CurrentUser, String i_CommitMessage, Commit prevSecondCommit) throws Exception
    {
        FolderDifferences differencesBetweenLastAndCurrentCommit = null;
        if (Folder.isDirEmpty(m_RepositoryPath))
        {
            throw new Exception("Can not creat new branch - there are no commits yet!");
        }

        if (ThereAreNoCmmitsYet())
        {
            createFirstCommitInActiveBranch(i_CurrentUser, i_CommitMessage);
            updateActiveBranchInFileSystemToPointToLatestCommit();
        } else
        {
            CreateANewCommitInActiveBranch(i_CurrentUser, i_CommitMessage, prevSecondCommit);
            if (this.m_ActiveBranch.getPointedCommit().GetPrevCommit() != null)
            {//it means there is no new commit because there were no changes
                differencesBetweenLastAndCurrentCommit = Commit.findDifferences(m_ActiveBranch.getPointedCommit(), m_ActiveBranch.getPointedCommit().GetPrevCommit());
                // System.out.println(differencesBetweenLastAndCurrentCommit);
                updateActiveBranchInFileSystemToPointToLatestCommit();
            }
        }
        return differencesBetweenLastAndCurrentCommit;
    }

    public boolean ThereAreNoCmmitsYet()
    {
        return getLastCommit() == null;
    }

    private void updateActiveBranchInFileSystemToPointToLatestCommit() throws IOException
    {
        Path branchPath = Paths.get(this.m_RepositoryPath.toString() + "\\.magit\\Branches\\" + this.m_ActiveBranch.getBranchName() + ".txt");
        writeToFileEraseTheOldOne(this.m_ActiveBranch.getPointedCommit().getSHA1(), branchPath);

    }

    //this method deletes the old file if there is one and write the new one
    private void writeToFileEraseTheOldOne(String i_Contnet, Path i_FilePathIncludingName) throws IOException
    {
        String addedContentForRTB = System.lineSeparator() + StringConstants.TRUE;

        i_Contnet = MagitFileUtils.IsRemoteTrackingBranch(i_FilePathIncludingName.toFile()) ?
                i_Contnet + addedContentForRTB : i_Contnet;

        MagitFileUtils.WritingFileByPath(i_FilePathIncludingName.toString(), i_Contnet);
    }

    private Commit getLastCommit()
    {
        return this.m_ActiveBranch.getPointedCommit();
    }

    private void CreateANewCommitInActiveBranch(User i_CurrentUser, String i_CommitMessage, Commit prevSecondCommit) throws Exception
    {
        Map<Path, Item> itemsMapWithPaths = new HashMap<>();
        // 1. create the root folder - current working copy
        Folder WC = Folder.createInstanceOfFolder(m_RepositoryPath, i_CurrentUser, itemsMapWithPaths);

        if (isThereSomethingToCommit(WC))
        {
            commitAllObjectsInAFolder(WC, i_CurrentUser);

            Path pathForWritingWC = WC.WritingFolderAsATextFile();
            zipAndPutInObjectsFolder(pathForWritingWC.toFile(), WC.getSHA1());
            Commit newCommit = createNewInstanceCommit(i_CurrentUser, WC, i_CommitMessage, prevSecondCommit);

            //putting Commit in objects
            String contentOfCommit = newCommit.CreatingContentOfCommit();
            Path pathForWritingCommit = WritingStringInAFile(contentOfCommit, newCommit.getSHA1());
            zipAndPutInObjectsFolder(pathForWritingCommit.toFile(), newCommit.getSHA1());

            // assign the new commit to be the current commit in the repository
            m_ActiveBranch.setPointedCommit(newCommit);
        } else
        {
            throw new Exception("There is nothing to commit in repository!");
        }
    }

    // this method creats recursivly the working copy as textFiles zipped inside objects as their names are their sha1
    private void createFirstCommitInActiveBranch(User i_CurrentUser, String i_CommitMessage) throws Exception
    {
        CreateANewCommitInActiveBranch(i_CurrentUser, i_CommitMessage, null);
    }

    //this method takes a Folder object and turns it in to a String -
    //than it writes to a file its content=theString
    //returns a Path to the created textFile of the Folder Object
    public void commitAllObjectsInAFolder(Folder i_FolderToCommit, User i_CurrentUser) throws IOException
    {
        //error: might haven't been initialized. if haven't been initialized (in "else") it will be through exception anyway

        Path pathForCommitItem = null;

        for (Item item : i_FolderToCommit.getListOfItems())
        {
            if (item.getTypeOfFile() == Item.TypeOfFile.FOLDER)
            {
                commitAllObjectsInAFolder((Folder) item, i_CurrentUser);

                Folder FolderForCommit = (Folder) item;
                pathForCommitItem = FolderForCommit.WritingFolderAsATextFile();

            } else //item is type of blob
            {
                Blob blobForCommit = (Blob) item;
                pathForCommitItem = WritingStringInAFile(blobForCommit.getContent(), blobForCommit.getSHA1());
            }

            zipAndPutInObjectsFolder(pathForCommitItem.toFile(), item.getSHA1());
        }
    }

    public void zipAndPutInObjectsFolder(File i_File, String i_Sha1) throws IOException
    {
        String pathForSavingFile = m_ObjectsFolderPath.toString();
        try
        {
            zipAFile(i_File, pathForSavingFile, i_Sha1);
        } catch (IOException e)
        {
            throw e;
        }
    }

    private void zipAFile(File i_FileForZip, String i_PathForSavingFile, String i_FileName) throws IOException
    {
        File fileToZip = new File(i_FileForZip.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream(i_PathForSavingFile + "\\" + i_FileName);

        ZipOutputStream zipOut = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(fileToZip);

        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());

        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;

        while ((length = fis.read(bytes)) >= 0)
        {
            zipOut.write(bytes, 0, length);
        }

        zipOut.close();
        fis.close();
        fos.close();
    }

    private boolean isThereSomethingToCommit(Folder i_NewRootFolder)
    {
        if (ThereAreNoCmmitsYet()) // if it is null then it is the first time we are commiting in this repository
        {
            return true;
        } else
        {
            //if it is the root folder has the same sha1 as the new commits root folder then for sure nothing has changed
            boolean isEqualSha1 = (m_ActiveBranch.getPointedCommit().getRootFolder().getSHA1()).equals(i_NewRootFolder.getSHA1());
            if (isEqualSha1)
            {
                return false;
            } else
                return true;//something has changed. now we need to check what is that has changed
        }
    }

    public Path getRepositoryPath()
    {
        return this.m_RepositoryPath;
    }

    public Branch getActiveBranch()
    {
        return m_ActiveBranch;
    }

    //this method set an active branch, if it is a new one we add it to all branches list
    public void setActiveBranch(Branch i_ActiveBranch)
    {
        this.m_ActiveBranch = i_ActiveBranch;
    }

    public Path GetObjectsFolderPath()
    {
        return this.m_ObjectsFolderPath;
    }

    public Folder GetUpdatedWorkingCopy(User i_user) throws Exception
    {
        Map<Path, Item> map = new HashMap<Path, Item>();
        Folder wc = Folder.createInstanceOfFolder(this.m_RepositoryPath, i_user, map);
        return wc;
    }

    public List<Branch> getAllBranches()
    {
        return m_Branches;
    }

    public Path getBranchesFolderPath()
    {
        return m_BranchesFolderPath;
    }


    public void AddingNewBranchInRepository(String i_nameOfNewBranch, String i_SHA1OfCommit) throws IOException
    {
        Branch newBranchToAdd = new Branch(i_nameOfNewBranch, m_AllCommitsSHA1ToCommit.get(i_SHA1OfCommit));

        if(m_Branches == null)
        {
            m_Branches = new ArrayList<>();
        }

        m_Branches.add(newBranchToAdd);
        MagitFileUtils.WritingFileByPath(
                m_BranchesFolderPath + sf_Slash + i_nameOfNewBranch + sf_txtExtension, /*commitOfSHA1.getSHA1*/
                m_ActiveBranch.getPointedCommit().getSHA1()
        );
    }

    public Path GetTempFolderPath()
    {
        return m_TempFolderPath;
    }

    public List<String> getActiveBranchesNameList()
    {
        List<String> branchNameList = new ArrayList<>();
        this.getActiveBranches().forEach(branch -> branchNameList.add(branch.getBranchName()));
        return branchNameList;
    }

    public boolean isHeadBranch(String i_branchName)
    {
        if (getActiveBranch().getBranchName().equals(i_branchName))
            return true;
        else return false;
    }

    public Branch getBranchByName(String i_branchToFind)
    {
        List<Branch> branchList = getAllBranches();
        for (int i = 0; i < branchList.size(); i++)
        {
            if (branchList.get(i).getBranchName().equals(i_branchToFind))
                return branchList.get(i);

        }
        return null;
    }

    public void SetMergeConflictsInstance(MergeConflictsAndMergedItems i_mergeConflictsAndMergedItems)
    {
        m_ConflictsAndItems = i_mergeConflictsAndMergedItems;
    }

    public MergeConflictsAndMergedItems getConflictsItemsAndNames()
    {
        return m_ConflictsAndItems;
    }

    public ObservableList<String> GetAllConflictsNames()
    {
        return m_ConflictsAndItems.GetConflictItemsNames();
    }

    public List<String> getBranchPointed(Commit commit)
    {
        return getAllBranches()
                .stream()
                .filter(branch -> branch.getPointedCommit().getSHA1().equals(commit.getSHA1()))
                .map(branch -> branch.getBranchName())
                .collect(Collectors.toList());
    }

}


