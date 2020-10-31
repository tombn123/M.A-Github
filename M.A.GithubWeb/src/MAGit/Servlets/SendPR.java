package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import collaboration.LocalRepository;
import github.users.UserManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/pages/repositoryPage/pullrequest"})
public class SendPR extends HttpServlet
{
    private final String PULL_REQUEST_URL = "repositoryPage.html";
    private final String BRANCH_TARGET = "branchTargetName";
    private final String BRANCH_BASE = "branchBaseName";
    private final String MESSAGE = "message";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException
    {
        proccessRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        proccessRequest(request, response);
    }

    private void proccessRequest(HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("text/html");



        String message = request.getParameter(MESSAGE);
        String branchTargetName = request.getParameter(BRANCH_TARGET);
        String branchBaseName = request.getParameter(BRANCH_BASE);

        //need to get 3 params

        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        User loggedInUser = userManager.getUserByName(SessionUtils.getUsername(request));

        LocalRepository userRepository = (LocalRepository) loggedInUser.getUserEngine().getCurrentRepository();

        String[] pathToNotifiy = userRepository.getRemoteRepoRef().getRepoPath().toString().split("\\\\");
        String userToNotifyInString = pathToNotifiy[pathToNotifiy.length - 2];

        User userToNotify = userManager.getUserByName(userToNotifyInString);
        //we should have/get all the data for create new pull request to the notify user

        //and later the notify user will show the CARD that represent the pr
        try
        {
            ServletUtils.getEngineAdapter(getServletContext()).sendPullRequest(loggedInUser, userToNotify, message, branchBaseName, branchTargetName,
                    userRepository.getRemoteRepoRef().getName());
        } catch (Exception e)
        {
            response.setStatus(400);

        }

    }
}
