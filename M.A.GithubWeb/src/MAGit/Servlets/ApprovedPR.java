package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import github.PullRequestLogic;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/pages/repositoryPage/pull/pullRequest/approvedpr"})
public class ApprovedPR extends HttpServlet
{
    private final String PR_ID = "prID";

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
        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(SessionUtils.getUsername(request));

        int id = Integer.parseInt(prID);
        //and later the notify user will show the CARD that represent the pr
        try
        {
            PullRequestLogic pullRequest = ServletUtils.getEngineAdapter(getServletContext()).getPullRequestInstance(loggedInUser, id);
//            PullRequestNotification pullRequestNotification = pullRequest.getNotification();

            ServletUtils.getEngineAdapter(getServletContext()).fastForwardBaseToTarget(pullRequest, loggedInUser);
        } catch (Exception e)
        {
            response.setStatus(400);
        }
    }
}
