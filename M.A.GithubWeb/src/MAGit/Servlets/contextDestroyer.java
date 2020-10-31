package MAGit.Servlets;

import org.apache.commons.io.FileUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;

@WebListener
public class contextDestroyer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            FileUtils.deleteDirectory(new File("c:\\magit-ex3"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            FileUtils.deleteDirectory(new File("c:\\magit-ex3"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}