package de.hsrt.meti.pms.es;


import java.time.Instant;
import java.time.LocalDate;
import de.hsrt.meti.pms.core.Address;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;



/*
 MongoDB Java Driver doesn't support java.util.Optional<T> fields.
 Also, the driver doesn't support sum types (sealed class hierarchies), so use Type enum for Event distinction
*/
public final record PatientEvent 
(
  PatientEvent.Type type,
  String id,
  Gender gender,
  String givenName,
  String familyName,
  LocalDate birthDate,
  LocalDate dateOfDeath,
  String healthInsurance,
  Address address,
  Instant timestamp
){

  // No DELETED value needed because command "Patient.Delete"
  // is not stored as an event, as it triggers deletion of all events
  public enum Type { 
    CREATED, UPDATED
  }

}

