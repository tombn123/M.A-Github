package github.repository;

public class RepositoryData
{
    private String repositoryName;
    private String latestCommit;
    private String message;
    private String activeBranch;
    private String commitAmount;
    private String userName;

    public RepositoryData(String repositoryName, String latestCommit, String message, String activeBranch, String commitAmount ,String userName)
    {
        this.repositoryName = repositoryName;
        this.latestCommit = latestCommit;
        this.message = message;
        this.activeBranch = activeBranch;
        this.commitAmount = commitAmount;
        this.userName = userName;
    }
}
