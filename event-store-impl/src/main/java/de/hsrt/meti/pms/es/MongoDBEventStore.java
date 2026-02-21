package de.hsrt.meti.pms.es;


import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;
import com.mongodb.client.model.TimeSeriesGranularity;
import static com.mongodb.client.model.Filters.*;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import de.hsrt.meti.pms.core.EventStore;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;



final class MongoDBEventStore implements EventStore
{

  public static final class Provider implements EventStore.Provider
  {
    @Override
    public EventStore getInstance(){
      return instance();
    }
  }


  private final MongoCollection<MongoPatient> patients;
  private final MongoCollection<PatientEvent> patientEvents;


  private MongoDBEventStore(
    final MongoCollection<MongoPatient> patients,
    final MongoCollection<PatientEvent> patientEvents
  ){
    this.patients      = patients;
    this.patientEvents = patientEvents;
  }


  private static boolean collectionExists(
    String name,
    MongoDatabase db
  ){
    return
      db.listCollectionNames()
        .into(new ArrayList<>())
        .contains(name);
  }


  // Factory method
  private static MongoDBEventStore setup(){

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

    // Set up time-series collections
    if (!collectionExists("patientEvents",db)){
      db.createCollection(
        "patientEvents",
        new CreateCollectionOptions().timeSeriesOptions(
          new TimeSeriesOptions("timestamp")
            .metaField("id")
            .granularity(TimeSeriesGranularity.SECONDS)
        )
      );
    }


    return new MongoDBEventStore(
      db.getCollection("patients",MongoPatient.class),
      db.getCollection("patientEvents",PatientEvent.class)
    );

  }


  // Singleton pattern
  private static MongoDBEventStore INSTANCE = setup();

  static MongoDBEventStore instance(){
    return INSTANCE;
  }


  // Helper method to build a filter for documents with a given Id, i.e.
  // collection.find({ 'id': '<ID>'})
  private static <T> Bson withId(Id<T> id){
    return eq("id", id.value());
  }


  private static Id<Patient> patientId(){
    return new Id<Patient>(ObjectId.get().toHexString());
  }


  @Override
  public Patient process(Patient.Command cmd) throws Exception {

    return switch(cmd){

      case Patient.Create cr -> {

        var id = this.patientId();

        patientEvents.insertOne(
          new PatientEvent(
            PatientEvent.Type.CREATED,
            id.value(),
            cr.gender(), 
            cr.givenName(),
            cr.familyName(),
            cr.birthDate(),
            null,             // date of death not set upon creation 
            cr.healthInsurance(),
            cr.address(),
            Instant.now() 
          )
        );

        var patient =
          this.findPatient(id).orElseThrow();              

        // Add the Patient's snapshot to the query collection
        patients.insertOne(MongoPatient.from(patient));

        yield patient;
      }

      case Patient.Update up -> {

        patientEvents.insertOne(
          new PatientEvent(
            PatientEvent.Type.UPDATED,
            up.id().value(),
            up.gender().orElse(null),
            up.givenName().orElse(null),
            up.familyName().orElse(null),
            null,               // Birthdate not update-able
            up.dateOfDeath().orElse(null),
            up.healthInsurance().orElse(null),
            up.address().orElse(null),
            Instant.now() 
          )
        );

        var patient =
          this.findPatient(up.id()).orElseThrow();              

        // Update the Patient's snapshot in the query collection
        patients.findOneAndReplace(withId(patient.id()),MongoPatient.from(patient));

        yield patient;
      }

      case Patient.Delete del -> {

        var patient = this.findPatient(del.id());

        patientEvents.deleteMany(withId(del.id()));
        patients.deleteMany(withId(del.id()));

        yield patient.orElseThrow();

      }

    };

  }


  @Override
  public Optional<Patient> stateOfPatientAt(Id<Patient> id, Instant t){

    // Get all events of Pat. id which occurred before t
    var events =
      patientEvents.find(and(withId(id),lte("timestamp",t)));

    Optional<Patient> patient = Optional.empty();

    // Loop over events (already ordered chronologically, being a time-series collection)
    // and accumulate them by consecutively 
 
    for (PatientEvent event : events){
      switch(event.type()){

        case PatientEvent.Type.CREATED -> 
          // Create a Patient instance wrapped in an Optional
          patient =
            Optional.of(
              new Patient(
                new Id<>(event.id()),
                event.gender(),
                event.givenName(),
                event.familyName(),
                event.birthDate(),
                Optional.ofNullable(event.dateOfDeath()),
                event.healthInsurance(),
                event.address(),
                event.timestamp()
              )
            );

        case PatientEvent.Type.UPDATED -> 
          // Apply Update to the wrapped Patient instance 
          patient =
            patient.map(
              pat -> pat.apply(
                new Patient.Update(
                  new Id<>(event.id()),
                  Optional.ofNullable(event.gender()),
                  Optional.ofNullable(event.givenName()),
                  Optional.ofNullable(event.familyName()),
                  Optional.ofNullable(event.dateOfDeath()),
                  Optional.ofNullable(event.healthInsurance()),
                  Optional.ofNullable(event.address())
                )
              )
            );
      }
    }
    return patient;

  }


  @Override
  public Optional<Patient> findPatient(Id<Patient> id){

    // Implemented terms of stateOfPatientAt(...) at time now
    return stateOfPatientAt(id,Instant.now());
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


}
