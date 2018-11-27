package ph.devcsrj.bugreports.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

public class RunnableHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports( Object handler ) {
        return handler instanceof Runnable;
    }

    @Override
    public ModelAndView handle( HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler ) throws Exception {
        Runnable runnable = (Runnable) handler;
        runnable.run();
        return null;
    }

    @Override
    public long getLastModified( HttpServletRequest request, Object handler ) {
        return -1;
    }
}
