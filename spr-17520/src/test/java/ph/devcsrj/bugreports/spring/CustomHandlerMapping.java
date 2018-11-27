package ph.devcsrj.bugreports.spring;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

public class CustomHandlerMapping implements HandlerMapping {

    private Queue<HandlerExecutionChain> chains;

    CustomHandlerMapping() {
        chains = new LinkedBlockingQueue<>();
    }

    void add( HandlerExecutionChain chain ) {
        chains.add( chain );
    }

    @Override
    public HandlerExecutionChain getHandler( HttpServletRequest request ) throws Exception {
        return chains.poll();
    }
}
