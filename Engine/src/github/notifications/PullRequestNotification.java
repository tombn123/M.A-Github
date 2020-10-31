package github.notifications;

import java.util.Date;

public class PullRequestNotification extends Notification
{
    private Status status;
    private String userName;
    private String targetBranchName; // my branch
    private String baseBranchName; // merge to branch
    private String message;
    private int id;

    public PullRequestNotification(Date dateCreated, String repositoryName, Status status, String userName, String targetBranchName, String baseBranchName, String message, int id)
    {
        super(dateCreated, repositoryName);

        this.status = status;
        this.userName = userName;
        this.targetBranchName = targetBranchName;
        this.baseBranchName = baseBranchName;
        this.message = message;
        this.id = id;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getTargetBranchName()
    {
        return targetBranchName;
    }

    public String getBaseBranchName()
    {
        return baseBranchName;
    }

    public String getMessage()
    {
        return message;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public String createNotificationTemplate()
    {
        return String.format("Pull Request Of: " + repositoryName + System.lineSeparator() +
                "Status:  " + status.toString() + System.lineSeparator() +
                "Message:  " + message + System.lineSeparator());
    }

    public Status GetStatus(){
        return status;
    }

    public String GetUserName(){
        return userName;
    }

    public String GetTargetBranchName(){
        return targetBranchName;
    }
    public String GetBaseBranchName(){
        return baseBranchName;
    }
    public String GetMessage(){
        return message;
    }


}
