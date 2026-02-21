package de.hsrt.meti.pms.repo.jpa;


import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Embedded;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.AttributeOverride;
import lombok.Data;
import lombok.AllArgsConstructor;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Address;


// Adapter Class between core.Patient and JPA:
// JPA requires classes to have a default constructor, so it doesn't work with records,
// which have only a non-default constructor.
// Lombok is used to avoid boilerplate of manually writing getters/setters

@Data
@AllArgsConstructor(staticName = "of")
@Entity
@Table(name = "patients")
final class JPAPatient
{

  @Id
  private String id;

  @Column
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column
  private String givenName;

  @Column
  private String familyName;

  @Column
  private LocalDate birthDate; 

  @Column
  private LocalDate dateOfDeath; // Not Optional<LocalDate> because JPA can't deal with Optional. Will be null if undefined

  @Column
  private String healthInsurance;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "street",     column = @Column),
    @AttributeOverride(name = "house",      column = @Column),
    @AttributeOverride(name = "postalCode", column = @Column),
    @AttributeOverride(name = "city",       column = @Column)
  })
  private JPAAddress address;

  @Column
  private Instant lastUpdate;


  // Conversion function from core.Patient to JPAPatient
  static JPAPatient from(Patient patient){
    return JPAPatient.of(
      patient.id().value(), 
      patient.gender(), 
      patient.givenName(), 
      patient.familyName(), 
      patient.birthDate(), 
      patient.dateOfDeath().orElse(null), 
      patient.healthInsurance(), 
      JPAAddress.from(patient.address()), 
      patient.lastUpdate()
    );
  }


  // Inverse function from JPAPatient to core.Patient
  Patient revert(){
    return new Patient(
      new de.hsrt.meti.pms.core.Id<>(id),
      gender,
      givenName,
      familyName,
      birthDate,
      Optional.ofNullable(dateOfDeath),
      healthInsurance,
      new Address(
        address.street,
        address.house,
        address.postalCode,
        address.city
      ),
      lastUpdate
    );
  }

}
