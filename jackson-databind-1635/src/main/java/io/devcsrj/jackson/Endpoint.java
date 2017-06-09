package io.devcsrj.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        defaultImpl = EndpointImpl.class )
@JsonSubTypes( value = {

})
public abstract class Endpoint {

    @JsonProperty( "name" )
    public abstract String name();

    @JsonProperty( "type" )
    public abstract Type type();

    @Override
    public String toString() {
        return "Endpoint{" +
                "name='" + name() + '\'' +
                ", type=" + type() +
                '}';
    }

    public abstract static class Builder<B extends Builder<B>> {

        @JsonProperty( "name" )
        public abstract B name( String name );

        @JsonProperty( "type" )
        public abstract B type( Type type );

        public abstract Endpoint build();
    }

    public static enum Type {

        UNRECOGNIZED( "unrecognized" ) {
            @Override
            protected Class getConcreteClass() {
                return EndpointImpl.class;
            }
        };

        private final String key;

        private Type( String key ) {
            this.key = key;
        }

        protected abstract Class getConcreteClass();

        @JsonCreator
        public static Type fromKey( String key ) {
            for ( Type type : values() ) {
                if ( type.key.equals( key ) )
                    return type;
            }
            return UNRECOGNIZED;
        }

        @JsonValue
        public String getKey() {
            return key;
        }

    }

}
