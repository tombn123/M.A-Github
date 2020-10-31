package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Commit", urlPatterns = {"/pages/repositoryPage/commit"})
public class Commit extends HttpServlet
{
    private final String REPOSITORY_PAGE_URL = "repositoryPage.html";
    private final String COMMIT_MSG = "commitMsg";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException
    {

        String commitMessage = request.getParameter(COMMIT_MSG);

        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(SessionUtils.getUsername(request));

        try
        {
            ServletUtils.getEngineAdapter(getServletContext()).commitChanges(commitMessage, loggedInUser);
        } catch (Exception e)
        {
            response.setStatus(400);

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
    }
}
