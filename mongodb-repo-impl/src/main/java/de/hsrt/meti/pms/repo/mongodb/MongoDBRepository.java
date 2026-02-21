package de.hsrt.meti.pms.repo.mongodb;


import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.StreamSupport.stream;
import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.Optional;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClients;
import static com.mongodb.client.model.Filters.*;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.Period;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Address;
import de.hsrt.meti.pms.core.Repository;



class MongoDBRepository implements Repository
{

  // Service Provider Interface (SPI)  
  public static final class Provider implements Repository.Provider
  {

    @Override
    public Repository getInstance(){
      return instance();
    }

  }

  private final MongoCollection<MongoPatient> patients;

  private MongoDBRepository( 
    final MongoCollection<MongoPatient> patients
  ){
    this.patients = patients;
  }


  // Factory method
  private static MongoDBRepository setup(){

    var host =
      System.getProperty("pms.mongodb.host");

    var port =
      Optional.ofNullable(System.getProperty("pms.mongodb.port")).orElse("27017");

    var options =
      Optional.ofNullable(System.getProperty("pms.mongodb.options"));

    var uri =
      "mongodb://" + host + ":" + port + options.map(opt -> "/?" + opt).orElse("");

    var client =
      MongoClients.create(uri);

    var dbName = 
      Optional.ofNullable(System.getProperty("pms.mongodb.name")).orElse("pms-db");

    var db =
      client.getDatabase(dbName);

    return new MongoDBRepository(
      db.getCollection("patients",MongoPatient.class)
    );

  }


  // Singleton pattern
  private static MongoDBRepository INSTANCE = setup();

  static MongoDBRepository instance(){
    return INSTANCE;
  }
  
 
  // Helper method to build a filter for documents with a given Id, i.e.
  // collection.find({ 'id.value': '<ID>'})
  private static <T> Bson withId(Id<T> id){ 
    return eq("id", id.value());
  }
 

  @Override
  public Id<Patient> patientId(){
    return new Id<Patient>(ObjectId.get().toHexString());
  }


  @Override
  public void save(Patient patient) throws Exception {

    var id = patient.id();

    var inserted =
      findPatient(id).isPresent() ?
        patients.findOneAndReplace(withId(id),MongoPatient.from(patient)) :
        patients.insertOne(MongoPatient.from(patient));
  }


  @Override
  public Optional<Patient> findPatient(Id<Patient> id){
    return
      Optional.ofNullable(
        patients.find(withId(id)).first()
      )
      .map(MongoPatient::revert);
  }


  @Override
  public List<Patient> findPatients(Patient.Filter filter){

    // Convert Patient.Filter into MongoDB filter:
    List<Bson> criteria = new ArrayList<>();

    // Add present filter criteria to list
    filter.gender().ifPresent(set -> criteria.add(in("gender",set)));    
    filter.familyName().ifPresent(name -> criteria.add(regex("familyName",name)));
    filter.birthDatePeriod().ifPresent(
      period -> {
        criteria.add(gte("birthDate",period.start()));
        period.end().ifPresent(date -> criteria.add(lte("birthDate",date)));
      }
    );   
    //TODO: Address filter criteria... 
  
    // Submit query with criteria combined by 'and' logic (crit1 and crit2 and ...), if present
    var results =
      criteria.isEmpty() ?
        patients.find() :
        patients.find(and(criteria));

    return
      stream(results.spliterator(),false)
        .map(MongoPatient::revert)
        .collect(toList());
  }


  @Override
  public Optional<Patient> deletePatient(Id<Patient> id) throws Exception {

    var patient = findPatient(id);

    patients.deleteOne(withId(id)); 

    return patient;
  }

}
