package de.hsrt.meti.pms.core;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import static java.util.UUID.randomUUID;


class InMemRepository implements Repository
{

  // Service Provider Interface (SPI)
  public static final class Provider implements Repository.Provider
  {
    @Override
    public Repository getInstance(){
      return new InMemRepository();
    }

  }

  private final Map<Id<Patient>,Patient> patients;


  InMemRepository(){
    this.patients = new HashMap<>();
  }


  @Override
  public Id<Patient> patientId(){
    // Create new ID
    var id = new Id<Patient>(randomUUID().toString());

    // To be absolutely sure: Check if ID already is in use, then either recurse or return it;
    return patients.containsKey(id) ? patientId() : id;
  }


  @Override
  public void save(Patient patient) throws Exception {
    patients.put(patient.id(),patient);
  }


  @Override
  public Optional<Patient> findPatient(Id<Patient> id){

    return Optional.ofNullable(patients.get(id));
  }



  private static Predicate<Patient> toPredicate(Patient.Filter filter){ 
    return patient -> {
      return filter.gender().map(set -> set.contains(patient.gender())).orElse(true) &&
        filter.familyName().map(name -> patient.familyName().contains(name)).orElse(true) &&
        filter.birthDatePeriod().map(period -> period.contains(patient.birthDate(),(d1,d2) -> d1.compareTo(d2))).orElse(true) &&
        filter.address().map(
          address -> 
            address.street().map(s -> patient.address().street().contains(s)).orElse(true) &&
            address.city().map(s -> patient.address().city().contains(s)).orElse(true)
        ).orElse(true);
    };
  }

/*
  // Alternative imperative filter implementation
  private static Predicate<Patient> toPredicateAlternative(Patient.Filter filter){ 
    return patient -> {
      var genderMatches =
        filter.gender().isEmpty() ?
          true :
          filter.gender().get().contains(patient.gender());
      
      var vitalStatusMatches =
        filter.vitalStatus().isEmpty() ?
          true :
          filter.vitalStatus().get().contains(patient.vitalStatus());
      
      var nameMatches =
        filter.familyName().isEmpty() ?
          true :
          patient.familyName().contains(filter.familyName().get());

      var birthDateMatches =
        filter.birthDatePeriod().isEmpty() ?
          true :
          filter.birthDatePeriod().get().contains(patient.birthDate(),(d1,d2) -> d1.compareTo(d2));

      //TODO Address check
      
      return genderMatches && vitalStatusMatches && nameMatches && birthDateMatches;
    };
  }
*/

  @Override
  public List<Patient> findPatients(Patient.Filter filter){ 

    return
      patients.values()
        .stream()
        .filter(toPredicate(filter))
        .collect(toList());
  }

  @Override
  public Optional<Patient> deletePatient(Id<Patient> id) throws Exception {

    return Optional.ofNullable(patients.remove(id));
  }


}
