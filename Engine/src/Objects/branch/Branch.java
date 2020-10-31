package Objects.branch;

import Objects.Commit;
import Objects.Item;
import System.MergeConflictsAndMergedItems;
import XmlObjects.repositoryWriters.RepositoryWriter;
import collaboration.Fetch;
import common.MagitFileUtils;
import common.constants.NumConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Branch
{
    private String m_BranchName;
    private transient Commit m_PointedCommit;

    public Branch(String i_BranchName, Commit i_CurrentCommit)
    {
        m_PointedCommit = i_CurrentCommit;
        m_BranchName = i_BranchName;
    }

    public static String GetCommitHistory(Branch i_Branch, Path i_ObjectsFolder) throws IOException
    {
        //TODO: apply for second prev commit
        StringBuilder commitHistoryBuilder = new StringBuilder();
        String headline = "Commits details:\n";
        commitHistoryBuilder.append(headline);
        commitHistoryBuilder.append(Commit.GetInformation(i_Branch.getPointedCommit()));
        if (i_Branch.getPointedCommit().GetPrevCommit().getSHA1() != null)
        {
            if (!i_Branch.getPointedCommit().GetPrevCommit().getSHA1().equals("null"))
            {
                commitHistoryBuilder.append("Previous Commit:\n");
                Path prevCommitTextFileZipped = Paths.get(i_ObjectsFolder.toString() + "\\" + i_Branch.m_PointedCommit.

                        GetPrevCommit().getSHA1());
                Path PrevCommitTextFileUnzipped = Item.UnzipFile(prevCommitTextFileZipped, Paths.get(i_ObjectsFolder.getParent().toString() + "\\Temp"));
                String prevCommitsDetails = Commit.GetInformationFromCommitTextFile(i_Branch.m_PointedCommit.GetPrevCommit().getSHA1(), PrevCommitTextFileUnzipped, i_ObjectsFolder);
                commitHistoryBuilder.append(prevCommitsDetails);
            }
        }
        return commitHistoryBuilder.toString();
    }

    public static List<Branch> GetAllBranches(Path i_BranchFolderPath, Map<String, Commit> allCommits) throws Exception
    {
        List<Branch> allBranches = new ArrayList<Branch>();

        File[] allBranchesFiles = MagitFileUtils.GetFilesInLocation(i_BranchFolderPath.toString());/*i_BranchFolderPath.toFile().listFiles();*/
        for (File branchFile : allBranchesFiles)
        {
            if (!MagitFileUtils.IsHeadBranchFile(branchFile))
            {
                String SHA1OfCommit = MagitFileUtils.GetContentFile(branchFile);
                Commit commitToPointOn = allCommits.get(SHA1OfCommit);

                allBranches.add(new Branch(MagitFileUtils.RemoveExtension(branchFile.toPath()), commitToPointOn));
            }
        }

        return allBranches;
    }

      /*  for (int i = 0; i < allBranchesFiles.length; i++)
        {
            if (!allBranchesFiles[i].getName().equals(ResourceUtils.HEAD))
            {
                allBranches.add(Branch.createBranchInstanceFromExistBranch(allBranchesFiles[i].toPath()));
            }
        }
        return allBranches;*/


    public static Branch createBranchInstanceFromExistBranch(Path i_BranchesPath) throws Exception
    {
        String branchName;
        Path realPathToBranch = i_BranchesPath;
        // 1. get branch name
        // if the path is to the HEAD Branch then we want to extract the real branch name
        if (realPathToBranch.getFileName().getFileName().toString().equals("HEAD.txt"))
        {
            branchName = extractBranchName(i_BranchesPath);
            realPathToBranch = Paths.get(i_BranchesPath.getParent().toString() + "\\" + branchName + ".txt");
        } else
        {
            branchName = MagitFileUtils.RemoveExtension(i_BranchesPath);
        }

        String branchCommitsSha1 = Branch.getCommitSha1FromBranchFile(realPathToBranch);
        Path ObjectsFolderPath = Paths.get(realPathToBranch.getParent().getParent().toString() + "\\Objects");
        Commit branchCommit = Commit.CreateCommitFromSha1(branchCommitsSha1, ObjectsFolderPath);

        return new Branch(branchName, branchCommit);
    }


    //TODO: if there is more then one line throw exception
    private static String getCommitSha1FromBranchFile(Path i_Branch) throws FileNotFoundException
    {
        String commitsSha1 = null;
        Scanner lineScanner = new Scanner(i_Branch.toFile());
        while (lineScanner.hasNext())
        {
            commitsSha1 = lineScanner.nextLine();
        }
        return commitsSha1;
    }

    private static String extractBranchName(Path i_branchesPath) throws FileNotFoundException
    {
        Scanner lineScanner = new Scanner(i_branchesPath.toFile());
        String branchName = null;
        while (lineScanner.hasNext())
        {
            branchName = lineScanner.next();

        }
        return branchName;
    }

    public static Optional<Branch> GetHeadBranch(List<Branch> i_AllBranches, String headBranchName) throws Exception
    {
        Optional<Branch> headBranch = i_AllBranches
                .stream().
                        filter(branch -> branch.getBranchName().equals(headBranchName)).findFirst();
        return headBranch;
    }

    public MergeConflictsAndMergedItems GetConflictsForMerge(Branch i_PushingBranch, Path i_RepositoryPath, Map<String, Commit> i_allCommitsMap) throws Exception
    {
        MergeConflictsAndMergedItems mergeConflicts = Commit.GetConflictsForMerge(this.getPointedCommit(), i_PushingBranch.getPointedCommit(), i_RepositoryPath, i_allCommitsMap);
        return mergeConflicts;
    }

    public String getBranchName()
    {
        return m_BranchName;
    }

    public Commit getPointedCommit()
    {
        return m_PointedCommit;
    }

    public void setPointedCommit(Commit i_Commit)
    {
        m_PointedCommit = i_Commit;
    }

    public boolean AreTheSameBranches(Branch branch)
    {
        return m_PointedCommit.AreTheCommitsTheSame(branch.getPointedCommit()) &&
                branch.getBranchName().equals(m_BranchName);
    }

    public void getAllCommitsPointed(List<Commit> lst)
    {
        if (m_PointedCommit == null)
            return;

        if (m_PointedCommit.ThereIsPrevCommit(NumConstants.ONE))
        {
            m_PointedCommit.GetPrevCommit().addAlllHisPrevCommits(lst);
        }

        if (m_PointedCommit.ThereIsPrevCommit(NumConstants.TWO))
        {
            m_PointedCommit.GetSecondPrevCommit().addAlllHisPrevCommits(lst);
        }

        lst.add(m_PointedCommit);
    }
}