package de.hsrt.meti.pms.core;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;


public record PatientRecord
(
  @JsonProperty
  Patient patient,

  @JsonProperty
  List<Diagnosis> diagnoses,

  @JsonProperty
  List<Prescription> prescriptions
)
{

  public Id<Patient> id(){
     return patient.id();
  }



  // Examples for how PatientRecord could also be defined as "aggregate root", 
  // with commands for each constituting entity Patient, Diagnosis, ...
  
  public static sealed interface Command { }

  public static record Create
  (
    Gender gender,
    String givenName,
    String familyName,
    LocalDate birthDate,
    String healthInsurance,
    Address address
  )
  implements Command {}

  public static record UpdatePatientData
  (
    Id<Patient> id,
    Optional<Gender> gender,
    Optional<String> givenName,
    Optional<String> familyName,
    Optional<LocalDate> dateOfDeath,
    Optional<String> healthInsurance,
    Optional<Address> address
  )
  implements Command {}

  public static record AddDiagnosis
  (
    Id<Patient> patient,
    Coding coding
  )
  implements Command {}

  public static record UpdateDiagnosis
  (
    Id<Patient> patient,
    Id<Diagnosis> diagnosis,
    Coding coding
  )
  implements Command {}

  public static record AddPrescription
  (
    Id<Patient> patient,
    Id<Diagnosis> diagnosis,
    Coding coding
  )
  implements Command {}

  public static record UpdatePrescription
  (
    Id<Patient> patient,
    Id<Prescription> prescription,
    Coding coding
  )
  implements Command {}

  public static record Delete
  (
    Id<Patient> id
  )
  implements Command {}


}
