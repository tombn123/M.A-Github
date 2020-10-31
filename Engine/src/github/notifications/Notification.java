package github.notifications;

import java.util.Date;

public abstract class Notification
{
    protected Date dateCreated;
    protected String repositoryName;

    public Notification(Date dateCreated, String repositoryName)
    {
        this.dateCreated = dateCreated;
        this.repositoryName = repositoryName;
    }

    public Date getDateCreated()
    {
        return dateCreated;
    }

    public String getRepositoryName()
    {
        return repositoryName;
    }

    public abstract String createNotificationTemplate();
}
