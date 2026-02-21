package de.hsrt.meti.pms.repo.mongodb;


import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import de.hsrt.meti.pms.core.Address;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;


// Needed as workaround for the fact that MongoDB Java Driver doesn't support java.util.Optional<T> fields
public record MongoPatient
(
  String id,
  Gender gender,
  String givenName,
  String familyName,
  LocalDate birthDate,
  LocalDate dateOfDeath,
  String healthInsurance,
  Address address,
  Instant lastUpdate
)
{

  static MongoPatient from(Patient patient){
    return new MongoPatient(
      patient.id().value(),
      patient.gender(),
      patient.givenName(),
      patient.familyName(),
      patient.birthDate(),
      patient.dateOfDeath().orElse(null),
      patient.healthInsurance(),
      patient.address(),
      patient.lastUpdate()
    );
  }


  Patient revert(){
    return new Patient(
      new Id<Patient>(this.id),
      this.gender,
      this.givenName,
      this.familyName,
      this.birthDate,
      Optional.ofNullable(this.dateOfDeath),
      this.healthInsurance,
      this.address,
      this.lastUpdate
    );    
  }

}
