package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/pages/repositoryPage/pull/pullRequest/userinfo"})
public class UserInfoSupplier extends HttpServlet
{
    private final String REPOSITORY_PAGE_URL = "repositoryPage.html";
    private final String DATA_TYPE = "dataType";
    private final String PullRequests = "pullRequests";

    private void proccessRequest(HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("text/html");
        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(SessionUtils.getUsername(request));

        String dataType = request.getParameter(DATA_TYPE);

        List<Object> dataRequested = null;

        switch (dataType)
        {
            case PullRequests:
                dataRequested = loggedInUser.getPullRequestLogicList()
                        .stream()
                        .map(pullRequest -> (Object) pullRequest.getNotification())
                        .collect(Collectors.toList());
                break;
        }

        try (PrintWriter out = response.getWriter())
        {
            Gson gson = new Gson();
            // get all the names of who ever was connected from directory
            String json = gson.toJson(dataRequested);
            out.println(json);
            out.flush();
        } catch (Exception e)
        {
            response.setStatus(400);

        }
    }

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

}
