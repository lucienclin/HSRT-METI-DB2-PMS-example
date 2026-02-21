package de.hsrt.meti.pms.core;


import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


// Dedicated ID type to clearly distinguish IDs from other mere Strings

@JsonSerialize(using = Id.Serializer.class)
public record Id<T>(String value)
{

  @Override public String toString(){ return value; }

 

  public static class Serializer<T> extends StdSerializer<Id<T>>
  {

    public Serializer(){
      this(null);
    }

    public Serializer(Class<Id<T>> cl){
      super(cl);
    }

    @Override
    public void serialize(
      Id<T> id,
      JsonGenerator json,
      SerializerProvider provider
    )
    throws IOException {
      json.writeString(id.value());
    }

  }

}
