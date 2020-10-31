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
import java.util.Collections;
import java.util.List;

@WebServlet(urlPatterns = {"/pages/repositoryPage/repositoryInfo"})
public class RepositoryInfoSupplier extends HttpServlet
{
    private final String REQUEST_TYPE = "requestType";
    private final String Branches = "1";
    private final String Commits = "2";
    private final String Names = "3";
    private final String PullRequest = "4";
    private final String IsLocalRepository = "5";
    private final String LocalBranches = "6";
    private final String IsWCDirty = "7";



    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        List<Object> dataRequested = null;
        String isDirtyWc;
        String dataType = request.getParameter(REQUEST_TYPE);
//        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getCurrentUser();

        try
        {
            User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(SessionUtils.getUsername(request));

            switch (dataType)
            {
                //randomly i decided that userName and repositoryName will return with branches
                case Branches:
                    dataRequested = ServletUtils.getEngineAdapter(getServletContext()).getBranchesList(loggedInUser);
                    break;
                case Commits:
                    dataRequested = ServletUtils.getEngineAdapter(getServletContext()).getCommitsData(loggedInUser);
                    Collections.reverse(dataRequested);
                    break;
                case Names:
                    dataRequested = ServletUtils.getEngineAdapter(getServletContext()).getRepositoryName(loggedInUser);
                    break;
                case PullRequest:
                    dataRequested = ServletUtils.getEngineAdapter(getServletContext()).getPullRequests(loggedInUser);
                    break;
                case IsLocalRepository:
                    dataRequested = ServletUtils.getEngineAdapter(getServletContext()).isLocalRepository(loggedInUser);
                    break;
                case LocalBranches:
                    dataRequested = ServletUtils.getEngineAdapter(getServletContext()).getLocalBrances(loggedInUser);
                    break;
                    case IsWCDirty:
                    isDirtyWc = ServletUtils.getEngineAdapter(getServletContext()).isDirtyWc(loggedInUser);
                        try (PrintWriter out = response.getWriter())
                        {
                            Gson gson = new Gson();
                            String json = gson.toJson(isDirtyWc);

                            out.println(json);
                            out.flush();
                        }
                    break;
            }
        } catch (Exception e)
        {
            response.setStatus(400);

        }

        try (PrintWriter out = response.getWriter())
        {
            Gson gson = new Gson();
            String json = gson.toJson(dataRequested);

            out.println(json);
            out.flush();
        }
        //todo:
        // for debugging, remove on finish
        catch (Exception e)
        {
            System.out.println("Problem requested:" + e.getMessage());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>
}
