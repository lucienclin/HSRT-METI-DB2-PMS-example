package de.hsrt.meti.pms.core;


import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;


public record Patient
(
  @JsonProperty
  Id<Patient> id,

  @JsonProperty
  Gender gender,

  @JsonProperty
  String givenName,

  @JsonProperty
  String familyName,

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  LocalDate birthDate,

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  Optional<LocalDate> dateOfDeath,

  @JsonProperty
  String healthInsurance,

  @JsonProperty
  Address address,

  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
  Instant lastUpdate
)
{

  public enum VitalStatus
  {
    ALIVE, DECEASED
  }

  
  public static sealed interface Command permits Create, Update, Delete { }

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


  // Partial Update, i.e. only properties to be updated/changed need be defined (see. below)
  public static record Update
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


  public static record Delete
  (
    Id<Patient> id
  )
  implements Command {} 


  public static final record Filter
  (
    Optional<Set<Gender>> gender,
    Optional<String> familyName,
    Optional<Period<LocalDate>> birthDatePeriod,
    Optional<Address.Filter> address
  )
  {
    public static final Filter NONE =
      new Filter(
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
      );
  }


  public static interface Operations
  {
    Patient process(Command cmd) throws Exception;

    Optional<Patient> getPatient(Id<Patient> id);
    
    List<Patient> findPatients(Filter filter);
  }



  public VitalStatus vitalStatus(){ 
    return dateOfDeath.isEmpty() ? VitalStatus.ALIVE : VitalStatus.DECEASED;
  }


  // Apply the (partial) Update to this Patient, i.e. create a copy of the Patient object
  // with each property modified according to the Update
  public Patient apply(Update update){
    return new Patient(
      this.id,
      update.gender().orElse(this.gender),
      update.givenName().orElse(this.givenName),
      update.familyName().orElse(this.familyName),
      this.birthDate,
      update.dateOfDeath().or(() -> this.dateOfDeath),
      update.healthInsurance().orElse(this.healthInsurance),
      update.address().orElse(this.address),
      Instant.now()
    );
  }


}

