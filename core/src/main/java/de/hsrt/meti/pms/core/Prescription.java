package de.hsrt.meti.pms.core;


import java.time.Instant;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;


public record Prescription
(
  @JsonProperty
  Id<Prescription> id,
  
  @JsonProperty
  Id<Patient> patient,

  @JsonProperty
  Id<Diagnosis> diagnosis,

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
  LocalDateTime recordedOn,

  @JsonProperty
  Coding medication,

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
    Coding medication
  ) 
  implements Command {}

  // The only attribute that might need to be changed is the medication coding, in course of a correction.
  // The others like "patient", "diagnosis" and "recordedOn" are in principle constant
  public static record Update
  (
    Id<Prescription> id,
    Coding medication
  ) 
  implements Command {}

  // No Delete command: Prescription entities would only be deleted
  // as part of the patient record, i.e. together with the Patient entity

}
