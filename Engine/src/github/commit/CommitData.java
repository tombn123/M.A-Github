package github.commit;

public class CommitData
{
    private String sha1;
    private String message;
    private String creator;
    private String branchPointed;

    public CommitData(String sha1, String message, String creator, String branchPointed)
    {
        this.sha1 = sha1;
        this.message = message;
        this.creator = creator;
        this.branchPointed = branchPointed;
    }
}
