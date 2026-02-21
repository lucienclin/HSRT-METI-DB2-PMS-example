package de.hsrt.meti.pms.repo.jpa;


import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import static java.util.stream.Collectors.toList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.Period;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Repository;


final class JPARepository implements Repository
{

  private static EntityManagerFactory emFactory =
    Persistence.createEntityManagerFactory("Patients_JPA");


  public static final class Provider implements Repository.Provider
  {

    @Override
    public Repository getInstance(){
      return new JPARepository(emFactory.createEntityManager());  
    }

  }


  private final EntityManager em;

 
  private JPARepository(EntityManager em){
    this.em = em;
  }



  @Override
  public Id<Patient> patientId(){

    var id = new Id<Patient>(UUID.randomUUID().toString());

    return findPatient(id).isEmpty() ? id : patientId();
  }


  @Override
  public void save(Patient patient) throws Exception {

    em.getTransaction().begin();
    em.persist(JPAPatient.from(patient));
    em.getTransaction().commit();

  }


  @Override
  public Optional<Patient> findPatient(Id<Patient> id){
    return
      Optional.ofNullable(
        em.find(JPAPatient.class, id.value())
      )
      .map(JPAPatient::revert);
  }


  @Override
  public List<Patient> findPatients(Patient.Filter filter){

    // Using JPA Criteria API:
    CriteriaBuilder cb = em.getCriteriaBuilder();

    CriteriaQuery<JPAPatient> query = cb.createQuery(JPAPatient.class);
    Root<JPAPatient> root = query.from(JPAPatient.class);    


    var criteria = new ArrayList<Predicate>();

    filter.gender().ifPresent(
      genderSet -> criteria.add(root.get("gender").in(genderSet))
    );

    filter.familyName().ifPresent(
      name -> criteria.add(cb.like(root.get("familyName"),name))
    );

    filter.birthDatePeriod().map(Period::start).ifPresent(
      date -> criteria.add(cb.greaterThanOrEqualTo(root.get("birthDate"),date))
    );

    filter.birthDatePeriod().flatMap(Period::end).ifPresent(
      date -> criteria.add(cb.lessThanOrEqualTo(root.get("birthDate"),date))
    );


    Predicate[] preds = new Predicate[criteria.size()];

    query.select(root).where(criteria.toArray(preds));

    return
      em.createQuery(query)
        .getResultList()
        .stream()
        .map(JPAPatient::revert)
        .collect(toList());
  }


  @Override
  public Optional<Patient> deletePatient(Id<Patient> id) throws Exception {

    var patient =
      findPatient(id);
  
    patient.ifPresent(
      p -> {
        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();

      }
    );

    return patient;

  }

}
