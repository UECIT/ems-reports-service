package uk.nhs.cdss.reports.config;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.client.api.IBasicClient;
import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.List;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ReferralRequest;

public interface FhirRestfulClient extends IBasicClient {

  @Search
  List<ReferralRequest> getReferralRequestsByEncounter(@RequiredParam(name= ReferralRequest.SP_CONTEXT)
      ReferenceParam contextParam);

  @Search
  List<Composition> getCompositionsByEncounter(@RequiredParam(name= Composition.SP_ENCOUNTER)
      ReferenceParam encounterParam);

  @Read
  Encounter getEncounter(@IdParam IdType id);

  @Read
  Patient getPatient(@IdParam IdType id);

}
