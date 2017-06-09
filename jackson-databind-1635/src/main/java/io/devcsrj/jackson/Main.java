package io.devcsrj.jackson;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    public static void main( String[] args ) throws IOException {
        ObjectMapper om = new ObjectMapper();
        String json = "{\"type\":\"ggwp\",\"name\":\"Hello\"}";
        Endpoint endpoint = om.readValue( json, Endpoint.class );
        System.out.println( endpoint);

    }
}
