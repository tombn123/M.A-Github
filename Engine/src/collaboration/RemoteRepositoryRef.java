package collaboration;

import common.MagitFileUtils;
import common.constants.ResourceUtils;
import common.constants.StringConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RemoteRepositoryRef
{
    private String m_Name;
    private Path m_RepoPath;

    public RemoteRepositoryRef(String i_Name, Path i_RepoPath)
    {
        this.m_Name = i_Name;
        this.m_RepoPath = i_RepoPath;
    }

    public static RemoteRepositoryRef CreateRepositoryRefFromFile(String repositoryPath) throws IOException
    {
        String textFileRepoPath = repositoryPath + ResourceUtils.AdditinalPathMagit + ResourceUtils.Slash +
                StringConstants.REPOSITORY_DETAILS + ResourceUtils.TxtExtension;

        Path pathToRepoDetails = Paths.get(textFileRepoPath);

        List<String> remoteRepoDetails = MagitFileUtils.GetTextLines(pathToRepoDetails);

        return new RemoteRepositoryRef(remoteRepoDetails.get(0), Paths.get(remoteRepoDetails.get(1)));
    }

    public String getName()
    {
        return m_Name;
    }

    public Path getRepoPath()
    {
        return m_RepoPath;
    }
}
