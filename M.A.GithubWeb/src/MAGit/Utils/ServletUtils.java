package MAGit.Utils;

import MAGit.EngineAdapter;
import github.users.UserManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import static MAGit.Constants.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String ENGINE_ADAPTER_ATTRIBUTE_NAME = "engine";

    /*
    Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
    the actual fetch of them is remained un-synchronized for performance POV
     */
    private static final Object userManagerLock = new Object();
    private static final Object engineAdapterLock = new Object();

    public static UserManager getUserManager(ServletContext servletContext) {
        if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
            synchronized (userManagerLock) {
                if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                    servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
                }
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static EngineAdapter getEngineAdapter(ServletContext servletContext) throws Exception
    {
        if (servletContext.getAttribute(ENGINE_ADAPTER_ATTRIBUTE_NAME) == null) {
            synchronized (engineAdapterLock) {
                if (servletContext.getAttribute(ENGINE_ADAPTER_ATTRIBUTE_NAME) == null) {
                    EngineAdapter newEngineAdapter = new EngineAdapter();
                    servletContext.setAttribute(ENGINE_ADAPTER_ATTRIBUTE_NAME, newEngineAdapter);
                    newEngineAdapter.createMainFolder();
                }
            }
        }
        return (EngineAdapter) servletContext.getAttribute(ENGINE_ADAPTER_ATTRIBUTE_NAME);
    }

    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return INT_PARAMETER_ERROR;
    }
}
