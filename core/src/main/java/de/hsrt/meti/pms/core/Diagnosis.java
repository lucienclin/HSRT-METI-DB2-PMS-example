package de.hsrt.meti.pms.core;


import java.util.Optional;
import java.util.Set;
import java.time.Instant;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;


public record Diagnosis
(
  @JsonProperty
  Id<Diagnosis> id,

  @JsonProperty
  Id<Patient> patient,

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
  LocalDateTime recordedOn,

  @JsonProperty
  Coding coding,

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
  Instant lastUpdate
)
{

  public static sealed interface Command permits Create, Update {}

  // Attributes "recordedOn" and "lastUpdate" are set by application, so not represented here
  public static record Create
  (
    Id<Patient> patient,
    Coding coding
  ) 
  implements Command {}


  // The only attribute that might need to be changed is the "coding", in course of a correction.
  // The others like "patient" and "recordedOn" are in principle constant
  public static record Update
  (
    Id<Diagnosis> id,
    Coding coding
  ) 
  implements Command {}

  // No Delete command: Diagnosis entities would only be deleted
  // as part of the patient record, i.e. together with the Patient entity   

}
