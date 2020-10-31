package MAGit.Servlets;

import MAGit.Constants.Constants;
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
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(urlPatterns = {"/pages/repositoryPage/ShowCommitFileSystemServlet"})
public class ShowCommitFileSystemServlet extends HttpServlet
{

    // urls that starts with forward slash '/' are considered absolute
    // urls that doesn't start with forward slash '/' are considered relative to the place where this servlet request comes from
    // you can use absolute paths, but then you need to build them from scratch, starting from the context path
    // ( can be fetched from request.getContextPath() ) and then the 'absolute' path from it.
    // Each method with it's pros and cons...

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @SuppressWarnings("Duplicates")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String usernameFromSession = SessionUtils.getUsername(request);
        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(usernameFromSession);
        String commitSha1 = request.getParameter(Constants.COMMIT_SHA1);
        if (request.getParameter(Constants.IS_ROOT_FOLDER).equals("true"))
        {
            response.setContentType("application/json");
            try (PrintWriter out = response.getWriter())
            {
                Gson gson = new Gson();
                Object itemInfo = null;
                try
                {
                    itemInfo = ServletUtils.getEngineAdapter(getServletContext()).GetWorkingCopyItemInfoByCommit(loggedInUser, commitSha1);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                String json = gson.toJson(itemInfo);
                out.println(json);
                out.flush();
            }
        } else
        {
            response.setContentType("application/json");
            try (PrintWriter out = response.getWriter())
            {
                Gson gson = new Gson();
                Object itemInfo = null;
                try
                {
                    Path itemPath = Paths.get(request.getParameter(Constants.PATH));
                    itemInfo = ServletUtils.getEngineAdapter(getServletContext()).getItemInfoByPathAndCommit(itemPath, loggedInUser, commitSha1);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                String json = gson.toJson(itemInfo);
                out.println(json);
                out.flush();


            }
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

