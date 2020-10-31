package MAGit.Servlets;

import MAGit.Constants.Constants;
import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;
import com.google.gson.Gson;
import github.PullRequestLogic;
import github.repository.RepositoryData;
import github.users.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ALL")
public class AllRepositoriesInfoServlet extends HttpServlet {

    private final String FORK_URL = "../fork/fork.html";

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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        List<RepositoryData> currUserRepositories = new ArrayList<>();
        // todo: go through all users and appened all repositories - except current user
        UserManager manager = ServletUtils.getUserManager(getServletContext());
        User requestedUserName = manager.getUserByName(request.getParameter(Constants.USERNAME));

        try {
            currUserRepositories = ServletUtils.getEngineAdapter(getServletContext()).buildAllUsersRepositoriesData(requestedUserName, true);
        } catch (Exception e) {
            response.setStatus(400);
        }

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        String json = gson.toJson(currUserRepositories);
        out.println(json);
        out.flush();
        out.close();



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
            throws ServletException, IOException {
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
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
