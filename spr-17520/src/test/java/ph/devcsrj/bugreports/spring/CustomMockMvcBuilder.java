package ph.devcsrj.bugreports.spring;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;

public class CustomMockMvcBuilder extends StandaloneMockMvcBuilder {

    private static final Method ADD_BEAN;

    private Supplier<HandlerMapping> customHandlerMapping;
    private Supplier<HandlerAdapter> customHandlerAdapter;

    static {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(
                "org.springframework.test.web.servlet.setup.StubWebApplicationContext" );
        } catch ( ClassNotFoundException e ) {
            throw new AssertionError( e );
        }
        Method method = findMethod( clazz, "addBean", String.class, Object.class );
        method.setAccessible( true );
        ADD_BEAN = method;
    }

    CustomMockMvcBuilder addHandlerMapping( Supplier<HandlerMapping> handlerMappingSupplier ) {
        this.customHandlerMapping = handlerMappingSupplier;
        return this;
    }

    CustomMockMvcBuilder addHandlerAdapter( Supplier<HandlerAdapter> handlerAdapterSupplier ) {
        this.customHandlerAdapter = handlerAdapterSupplier;
        return this;
    }

    protected WebApplicationContext initWebAppContext() {
        WebApplicationContext context = super.initWebAppContext();
        if ( customHandlerMapping != null )
            invokeMethod( ADD_BEAN, context, "customHandlerMapping", customHandlerMapping.get() );

        if ( customHandlerAdapter != null )
            invokeMethod( ADD_BEAN, context, "customHandlerAdapter", customHandlerAdapter.get() );

        return context;
    }
}
