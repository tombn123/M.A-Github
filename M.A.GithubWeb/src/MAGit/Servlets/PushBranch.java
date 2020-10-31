package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import github.users.UserManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PushBranch", urlPatterns = {"/pages/repositoryPage/pushBranch"})
public class PushBranch extends HttpServlet
{
    private final String PULL_REQUEST_URL = "repositoryPage.html";
    private final String BRANCH_NAME = "branchName";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException
    {
        String branchToPushName = request.getParameter(BRANCH_NAME);

        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        User loggedInUser = userManager.getUserByName(SessionUtils.getUsername(request));

        try
        {
            ServletUtils.getEngineAdapter(getServletContext()).pushBranch(branchToPushName, loggedInUser, userManager);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println(branchToPushName);

        response.sendRedirect(PULL_REQUEST_URL);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
    }

}
