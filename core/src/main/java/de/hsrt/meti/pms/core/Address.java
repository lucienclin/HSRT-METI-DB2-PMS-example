package de.hsrt.meti.pms.core;


import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;


public record Address
(
  @JsonProperty String street,
  @JsonProperty String house,
  @JsonProperty String postalCode,
  @JsonProperty String city
)
{

  public static record Filter
  (
    Optional<String> street,
    Optional<String> city
  )
  {}


}
