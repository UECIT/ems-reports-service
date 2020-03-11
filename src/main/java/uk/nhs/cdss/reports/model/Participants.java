package uk.nhs.cdss.reports.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.RelatedPerson;

@Value
@Builder
public class Participants {

  @Builder.Default
  private final List<Practitioner> practitioners = new ArrayList<>();
  @Builder.Default
  private final List<RelatedPerson> relatedPeople = new ArrayList<>();

}
