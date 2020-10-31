package MAGit.Servlets;

import MAGit.Constants.Constants;
import MAGit.Utils.ServletUtils;
import MAGit.Utils.SessionUtils;
import System.Users.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;

@WebServlet(urlPatterns = {"/pages/repositoryPage/ChangeFileServlet"})
public class ChangeFileServlet extends HttpServlet {

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
            throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        User loggedInUser = ServletUtils.getUserManager(getServletContext()).getUserByName(usernameFromSession);


        if (request.getParameter(Constants.CHANGE_OR_DELETE).equals("delete")) {
            String filePathToDelete = request.getParameter(Constants.PATH);
            try {
                ServletUtils.getEngineAdapter(getServletContext()).RemoveFileFromWorkingCopy(Paths.get(filePathToDelete), loggedInUser);
            } catch (Exception e) {
                response.setStatus(400);
            }
        }
        else if(request.getParameter(Constants.CHANGE_OR_DELETE).equals("change")){
            String filePathToChange = request.getParameter(Constants.PATH);
            String newContentOfFile = request.getParameter(Constants.NEW_CONTENT);
            try {
                ServletUtils.getEngineAdapter(getServletContext()).ChangeFileInWorkingCopy(Paths.get(filePathToChange),newContentOfFile);
            } catch (Exception e) {
                response.setStatus(400);
            }

        }
        else if(request.getParameter(Constants.CHANGE_OR_DELETE).equals("new")){
            String filePathToPutTheNewFile = request.getParameter(Constants.PATH);
            String newFileName = request.getParameter(Constants.NEW_NAME);
            String newContentOfFile = request.getParameter(Constants.NEW_CONTENT);
            try {
                ServletUtils.getEngineAdapter(getServletContext()).CreateNewFileInPath(Paths.get(filePathToPutTheNewFile),newContentOfFile,newFileName);
            } catch (Exception e) {
                response.setStatus(400);
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

