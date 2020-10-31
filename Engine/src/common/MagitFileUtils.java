package common;

import common.constants.NumConstants;
import common.constants.ResourceUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MagitFileUtils
{
    public static void OverwriteContentInFile(String i_Content, String i_PathOfFile) throws IOException
    {
        File fileToWrite = new File(i_PathOfFile);

        FileWriter fileWriter = new FileWriter(fileToWrite, false);

        fileWriter.write(i_Content);
        fileWriter.close();
    }

    public static void CreateDirectory(String i_PathToMakeDir)
    {
        File tempFileForMakingDir = new File(i_PathToMakeDir);

        if (!tempFileForMakingDir.exists())
            tempFileForMakingDir.mkdir();
    }

    public static void WritingFileByPath(String i_PathForWriting, String i_ContentTWrite) throws IOException
    {
        File newFileToWrite = new File(i_PathForWriting);

        Path fixedPathFile = Paths.get(i_PathForWriting);

        if (!newFileToWrite.exists())
            Files.createFile(fixedPathFile);

        FileUtils.writeStringToFile(fixedPathFile.toFile(), i_ContentTWrite, "UTF-8");
    }

    public static boolean IsMagitFolder(File file)
    {
        return file.getName().equals(".magit");
    }

    public static boolean IsFolderExist(Path i_BranchFolderPath)
    {
        File[] branches = GetFilesInLocation(i_BranchFolderPath.toString());

        return Arrays.stream(branches).anyMatch(file ->
                file.isDirectory());
    }

    public static boolean IsRemoteRepositoryExistInLocation(String location)
    {
        //Path locationPath = Paths.get(location);
        File[] WC = GetFilesInLocation(location);

        if (magitFileExist(WC))
        {
            location += ResourceUtils.AdditinalPathBranches;
            return IsFolderExist(Paths.get(location));
        }
        return false;
    }

    public static File[] GetFilesInLocation(String location)
    {
        File repositoryFile = new File(location);
        return repositoryFile.listFiles();
    }

    private static boolean magitFileExist(File[] wc)
    {
        return Arrays.stream(wc).anyMatch(file ->
                IsMagitFolder(file));
    }

    public static List<String> GetTextLines(Path i_TextFilePath) throws IOException
    {
        Scanner lineScanner = new Scanner(i_TextFilePath);
        List<String> textFileLines = new ArrayList<>();

        while (lineScanner.hasNext())
        {
            textFileLines.add(lineScanner.nextLine());
        }
        return textFileLines;
    }

    public static boolean IsRemoteTrackingBranch(File branchFile) throws IOException
    {
        Path pathBranchFile = branchFile.toPath();

        return GetTextLines(pathBranchFile).size() == NumConstants.TWO;
    }

    public static String RemoveExtension(Path filePath)
    {
        String fileName;
        String[] fileNameAndExtension = filePath.getFileName().toString().split("\\.(?=[^\\.]+$)");
        fileName = fileNameAndExtension[0];
        return fileName;
    }

    public static boolean IsHeadBranchFile(File branchFile)
    {
        return branchFile.getName().equals(ResourceUtils.HEAD);
    }

    public static String GetContentFile(File i_file) throws Exception
    {
        try
        {
            String content = new String(Files.readAllBytes(Paths.get(i_file.getAbsolutePath())), StandardCharsets.UTF_8);
            return content;

        } catch (IOException e)
        {
            throw new Exception("Exception was occured, problem in reading file:" + i_file.getName());
        }
    }

    public static void WritingStringInFileWholePath(String filepath, String contentToWrite) throws IOException
    {
        File file = CreateWholePathDirecories(filepath);

        WritingFileByPath(file.getAbsolutePath(), contentToWrite);
    }

    public static File CreateWholePathDirecories(String filepath)
    {
        File file = new File(filepath);
        file.getParentFile().mkdirs();
        return file;
    }
}
