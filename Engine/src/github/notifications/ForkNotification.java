package github.notifications;

import Objects.Item;

import java.util.Date;

public class ForkNotification extends Notification
{
    private String userName;

    public ForkNotification(Date dateCreated, String repositoryName, String userName)
    {
        super(dateCreated, repositoryName);
        this.userName = userName;
    }

    @Override
    public String createNotificationTemplate()
    {
        return String.format(userName + " forked " + repositoryName + " on " + Item.getDateStringByFormat(dateCreated));
    }
}
