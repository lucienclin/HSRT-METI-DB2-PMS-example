package de.hsrt.meti.pms.core;


import java.util.Optional;


public record Coding
(
  String code,
  Optional<String> display,
  String system,
  Optional<String> version
)
{

  public static Coding of(
    String code,
    String display,
    String system,
    Optional<String> version
  ){
    return new Coding(code,Optional.of(display),system,version);
  }

}
