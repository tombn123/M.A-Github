package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import github.PullRequestLogic;
import github.notifications.PullRequestNotification;
import github.notifications.Status;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@WebServlet(urlPatterns = {"/pages/repositoryPage/pull/pullRequest/sendnotification"})
public class SendNotification extends HttpServlet
{
    private final String PR_ID = "prID";
    private final String STATUS = "status";
    private final String MESSAGE = "message";
    private final String DENIED_PARAM = "Denied";

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
        //get 3 params - message, id and status
        String prID = request.getParameter(PR_ID);
        Status prStatus = request.getParameter(STATUS).equals(DENIED_PARAM) ? Status.DENIED : Status.CONFIRMED;
        String messageOfPR = request.getParameter(MESSAGE);

        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(SessionUtils.getUsername(request));

        int id = Integer.parseInt(prID);
        //and later the notify user will show the CARD that represent the pr
        try
        {
            PullRequestLogic pullRequest = ServletUtils.getEngineAdapter(getServletContext()).getPullRequestInstance(loggedInUser, id);
            PullRequestNotification pullRequestNotification = pullRequest.getNotification();

            pullRequest.getUserSender().getNotificationList().add(new PullRequestNotification(
                    new Date(), pullRequestNotification.getRepositoryName(), prStatus, loggedInUser.getUserName(),
                    pullRequestNotification.getTargetBranchName(), pullRequestNotification.getBaseBranchName(), messageOfPR, id));

            loggedInUser.getNotificationList().remove(pullRequestNotification);

            response.sendRedirect("pullRequest.html");
        } catch (Exception e)
        {
            response.setStatus(400);

        }
    }
}
