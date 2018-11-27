package ph.devcsrj.bugreports.spring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;

import ph.devcsrj.bugreports.spring.MvcTest.Config;

@RunWith( SpringRunner.class )
@SpringBootTest( classes = Config.class )
public class MvcTest {

    @Configuration
    static class Config {

        @Bean
        CustomHandlerMapping handlerMapping() {
            return new CustomHandlerMapping();
        }

        @Bean
        RunnableHandlerAdapter handlerAdapter() {
            return new RunnableHandlerAdapter();
        }
    }

    private MockMvc mockMvc;
    @Autowired
    private CustomHandlerMapping handlerMapping;
    @Autowired
    private RunnableHandlerAdapter handlerAdapter;

    @Before
    public void before() {
        mockMvc = new CustomMockMvcBuilder()
            .addHandlerMapping( () -> handlerMapping )
            .addHandlerAdapter( () -> handlerAdapter )
            .build();
    }


    @Test
    public void customHandlerMappingWithCustomHandlerAdapter() throws Exception {
        CountDownLatch latch = new CountDownLatch( 1 );
        Runnable runnable = latch::countDown;
        handlerMapping.add( new HandlerExecutionChain( runnable ) );

        mockMvc.perform( get( "/" ) )
            .andExpect( status().isOk() );
        assertThat( latch.await( 100, TimeUnit.MILLISECONDS ), is( equalTo( true ) ) );
    }


    @Test
    public void customHandlerMappingWithRequestMappingHandlerAdapter() throws Exception {
        CountDownLatch latch = new CountDownLatch( 1 );
        CountDownLatchWrapper wrapper = new CountDownLatchWrapper( latch );
        HandlerMethod handlerMethod = new HandlerMethod( wrapper, "handle", HttpServletResponse.class );
        handlerMapping.add( new HandlerExecutionChain( handlerMethod ) );

        mockMvc.perform( get( "/" ) )
            .andExpect( status().isOk() );
        assertThat( latch.await( 100, TimeUnit.MILLISECONDS ), is( equalTo( true ) ) );
    }

    static class CountDownLatchWrapper {

        private final CountDownLatch latch;

        private CountDownLatchWrapper( CountDownLatch latch ) {
            this.latch = latch;
        }

        public void handle( HttpServletResponse response ) {
            latch.countDown();
            response.setStatus( 200 );
        }

    }
}
