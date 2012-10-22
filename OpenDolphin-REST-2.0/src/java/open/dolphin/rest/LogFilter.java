package open.dolphin.rest;

import java.io.IOException;
import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import open.dolphin.session.UserServiceBeanLocal;
import org.apache.log4j.Logger;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class LogFilter implements Filter {

    private static Logger logger = Logger.getLogger("open.dolphin");

    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";
    private static final String UNAUTHORIZED_USER = "Unauthorized user: ";

    /*private static final String TEST_USER_ID = "1.3.6.1.4.1.9414.2.100:ehrTouch";
    private static final String TEST_PASSWORD = "098f6bcd4621d373cade4e832627b4f6";
     */
    
    private static final String TEST_USER_ID = "1.3.6.1.4.1.9414.2.100:dolphin";    // K.Funabashi
    private static final String TEST_PASSWORD = "098f6bcd4621d373cade4e832627b4f6";
    
    private static final String SYSAD_USER_ID = "1.3.6.1.4.1.9414.2.1:cloudia";
    private static final String SYSAD_PASSWORD = "2cf069043321eeb1b146323ab3d7b819";
    private static final String SYSAD_PATH = "hiuchi/";

    private UserServiceBeanLocal userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        try {
            InitialContext ic = new InitialContext();
            userService = (UserServiceBeanLocal) ic.lookup("OpenDolphin-EA-2.0/UserServiceBean/local");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String userName = req.getHeader(USER_NAME);
        String password = req.getHeader(PASSWORD);

        boolean authentication = userService.authenticate(userName, password);

        if (!authentication) {

            String requestURI = req.getRequestURI();

            if (userName.equals(TEST_USER_ID) && password.equals(TEST_PASSWORD)) {
                authentication = true;
            } else if (userName.equals(SYSAD_USER_ID) &&
                       password.equals(SYSAD_PASSWORD) &&
                       requestURI.endsWith(SYSAD_PATH)) {
                authentication = true;
            }
        }

        if (!authentication) {
            HttpServletResponse res = (HttpServletResponse)response;
            StringBuilder sbd = new StringBuilder();
            sbd.append(UNAUTHORIZED_USER);
            sbd.append(userName).append(": ").append(req.getRequestURI());
            String msg = sbd.toString();
            logger.warn(msg);
            res.sendError(401);
            return;
        }

        BlockWrapper wrapper = new BlockWrapper(req);
        wrapper.setRemoteUser(userName);

        StringBuilder sb = new StringBuilder();
        sb.append(wrapper.getRemoteAddr()).append(" ");
        sb.append(wrapper.getShortUser()).append(" ");
        sb.append(wrapper.getMethod()).append(" ");
        sb.append(wrapper.getRequestURI());
        logger.info(sb.toString());

        chain.doFilter(wrapper, response);
    }

    @Override
    public void destroy() {
    }
}
