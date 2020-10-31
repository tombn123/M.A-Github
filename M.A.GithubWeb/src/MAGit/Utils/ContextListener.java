package MAGit.Utils;

import common.constants.ResourceUtils;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.nio.file.Paths;

@WebListener
public class ContextListener implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        System.out.println("Hello World");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        try
        {
            FileUtils.deleteDirectory(Paths.get(ResourceUtils.MainRepositoriesPath).toFile());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("Shutting down!");
    }
}
