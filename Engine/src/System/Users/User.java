package System.Users;

import System.Engine;
import common.constants.ResourceUtils;
import github.PullRequestLogic;
import github.notifications.Notification;

import java.util.ArrayList;
import java.util.List;

public class User
{
    private String userName;
    private List<Notification> notificationList;
    private List<PullRequestLogic> pullRequestLogicList;
    private boolean loggedIn;
    private transient Engine userEngine = new Engine();

    public User(String i_UserName)
    {
        userName = i_UserName;
        notificationList = new ArrayList<>();
        pullRequestLogicList = new ArrayList<>();
        loggedIn = true;
    }

    //need to check if need to reback to hold user in engine

    public List<PullRequestLogic> getPullRequestLogicList()
    {
        return pullRequestLogicList;
    }

    public Engine getUserEngine()
    {
        return userEngine;
    }

    public void setLoggedIn(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    public String buildUserPath()
    {
        return String.format(ResourceUtils.MainRepositoriesPath + ResourceUtils.Slash + userName);
    }

    public void addNotification(Notification notification)
    {
        notificationList.add(notification);
    }

    public void removedNotification(Notification notification)
    {
        notificationList.add(notification);
    }

    public List<Notification> getNotificationList()
    {
        return notificationList;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String i_UserName)
    {
        userName = i_UserName;
    }

    public List<String> prepareNotifications()
    {
        List<String> stringNotificationsToShow = new ArrayList<>();

        for (Notification notification : notificationList)
        {
            stringNotificationsToShow.add(notification.createNotificationTemplate());
        }

        return stringNotificationsToShow;
    }

    public boolean isLogIn()
    {
        return loggedIn;
    }

    public void clearNotifications()
    {
        notificationList.clear();
    }
}
