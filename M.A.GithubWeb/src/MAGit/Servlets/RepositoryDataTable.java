package MAGit.Servlets;

import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import com.google.gson.Gson;
import github.repository.RepositoryData;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = {"/repositorydata"})
public class RepositoryDataTable extends HttpServlet
{

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        String usernameFromSession = SessionUtils.getUsername(request);
        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(usernameFromSession);
        response.setContentType("application/json");
        List<RepositoryData> allRepositoriesData = null;
        PrintWriter out = response.getWriter();
        try
        {
            allRepositoriesData = ServletUtils.getEngineAdapter(getServletContext()).buildAllUsersRepositoriesData(loggedInUser, false);
        } catch (Exception e)
        {
            response.setStatus(400);

        }

        Gson gson = new Gson();

        String json = gson.toJson(allRepositoriesData);
        out.println(json);
        out.flush();
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }
}
