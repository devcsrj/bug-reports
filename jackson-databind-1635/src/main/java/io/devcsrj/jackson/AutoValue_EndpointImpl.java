package io.devcsrj.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AutoValue_EndpointImpl extends EndpointImpl {

    private final String name;
    private final Type type;

    private AutoValue_EndpointImpl( String name, Type type ) {
        this.name = name;
        this.type = type;
    }

    @JsonProperty( value = "name" )
    @Override
    public String name() {
        return name;
    }

    @JsonProperty( value = "type" )
    @Override
    public Endpoint.Type type() {
        return type;
    }

    static final class Builder extends EndpointImpl.Builder {

        private String name;
        private Endpoint.Type type;

        Builder() {
        }

        Builder( EndpointImpl source ) {
            this.name = source.name();
            this.type = source.type();
        }

        @Override
        public EndpointImpl.Builder name( String name ) {
            this.name = name;
            return this;
        }

        @Override
        public EndpointImpl.Builder type( Endpoint.Type type ) {
            this.type = type;
            return this;
        }

        @Override
        public EndpointImpl build() {
            String missing = "";
            if ( name == null ) {
                missing += " name";
            }
            if ( type == null ) {
                missing += " type";
            }
            if ( !missing.isEmpty() ) {
                throw new IllegalStateException( "Missing required properties:" + missing );
            }
            return new AutoValue_EndpointImpl(
                    this.name,
                    this.type );
        }

    }
}
