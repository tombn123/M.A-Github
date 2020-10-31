package github.users;

import System.Users.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
Adding and retrieving users is synchronized and in that manner - these actions are thread safe
Note that asking if a user exists (isUserExists) does not participate in the synchronization and it is the responsibility
of the user of this class to handle the synchronization of isUserExists with other methods here on it's own
 */
public class UserManager
{

    private final Set<User> usersSet;
    //private User currentUserName;

    public UserManager()
    {
        usersSet = new HashSet<>();
    }

    /*public void setCurrentUserName(User currentUserName)
    {
        AtomicBoolean isExist = new AtomicBoolean(false);
        usersSet.forEach(user ->
        {
            if (user.getUserName().equals(currentUserName.getUserName()))
            {
                this.currentUserName = user;
                isExist.set(true);
            }
        });
        if (isExist.get() == false)
            this.currentUserName = currentUserName;
    }*/

/*
    public void setCurrentUserName(String currentUserName)
    {
        this.currentUserName = usersSet.stream()
                .filter(user -> user.getUserName().equals(currentUserName))
                .findAny().orElse(null);
    }
*/

    public synchronized void addUser(User user)
    {
        usersSet.add(user);
    }

    public synchronized void removeUser(User user)
    {
        usersSet.remove(user);
    }

    public synchronized Set<User> getUsers()
    {
        return Collections.unmodifiableSet(usersSet);
    }

    public boolean isUserExists(String username)
    {
        Boolean exist = false;
        Iterator<User> iterator = usersSet.iterator();
        while (iterator.hasNext())
        {
            User currUser = (User) iterator.next();
            if (currUser.getUserName().equals(username))
                return true;
        }
        return false;
    }

    public User getUserByName(String i_UserNameToFind)
    {
        /*if (!isUserExists(i_UserNameToFind))
            throw new Exception("in function UserManager.GetUserByName - couldnt find the requested name");*/

        Iterator<User> userIterator = usersSet.iterator();
        while (userIterator.hasNext())
        {
            User currUser = userIterator.next();
            if (currUser.getUserName().equals(i_UserNameToFind))
                return currUser;
        }
        return null;
    }



    /*public Set<User> CreateUsersSetByNames(Set<String> i_NameList) {
        Set<User> userList = new HashSet<>();
        i_NameList.forEach(name -> {
            if(isUserExists(name)){
                try {
                    userList.add(GetUserByName(name));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
                userList.add(new User(name));
        });
        return userList;
    }*/

    /*public Set<User> CreateUsersSetByNamesWithoutCurrentUser(Set<String> i_NameList) {
        Set<User> userList = CreateUsersSetByNames(i_NameList);
        userList.remove(getCurrentUser());
        return userList;
    }*/
}
