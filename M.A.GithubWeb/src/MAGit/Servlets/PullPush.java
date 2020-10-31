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

@WebServlet(urlPatterns = {"/pages/repositoryPage/pullpush"})
public class PullPush extends HttpServlet
{
    private final String REPOSITORY_PAGE_URL = "repositoryPage.html";
    private final String PULL = "pull";
    private final String PUSH = "push";
    private final String COMMAND_TYPE = "commandType";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException
    {

        String commandType = request.getParameter(COMMAND_TYPE);

        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(SessionUtils.getUsername(request));

        try
        {
            switch (commandType)
            {
                case PULL:
                    ServletUtils.getEngineAdapter(getServletContext()).pull(loggedInUser);
                    break;

                case PUSH:
                    ServletUtils.getEngineAdapter(getServletContext()).push(loggedInUser);
                    break;
            }

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
