package io.devcsrj.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize( builder = AutoValue_EndpointImpl.Builder.class )
public abstract class EndpointImpl extends Endpoint {

    public static Builder builder() {
        return new AutoValue_EndpointImpl.Builder()
                .type( Type.UNRECOGNIZED );
    }

    public static abstract class Builder extends Endpoint.Builder<Builder> {

        @JsonCreator
        private static Builder create() {
            return EndpointImpl.builder();
        }

        public abstract EndpointImpl build();
    }

}
