package uk.nhs.cdss.reports.transform.ecds;

import static org.apache.commons.lang3.time.DateUtils.toCalendar;

import java.util.Calendar;
import org.hl7.fhir.dstu3.model.Procedure;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareInvestigationsSnomedCt;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;

@Component
public class EmergencyCareInvestigationsTransformer {


  public EmergencyCareInvestigationsSnomedCt[] transform(EncounterReportInput input) {
    if (input.getProcedures() == null) {
      return null;
    }

    return input.getProcedures().stream()
        .map(this::transform)
        .toArray(EmergencyCareInvestigationsSnomedCt[]::new);
  }

  private EmergencyCareInvestigationsSnomedCt transform(Procedure procedure) {
      EmergencyCareInvestigationsSnomedCt careInvestigation = EmergencyCareInvestigationsSnomedCt.Factory.newInstance();

    careInvestigation.setEmergencyCareClinicalInvestigationSnomedCt(procedure.getReasonCodeFirstRep().getCodingFirstRep().getCode());

    if (procedure.hasPerformedDateTimeType()) {
      Calendar performed = toCalendar(procedure.getPerformedDateTimeType().getValue());

      careInvestigation.xsetProcedureDateEmergencyCareClinicalInvestigation(
          DateTimeFormatter.formatDate(performed, DateType.type));
      careInvestigation.xsetProcedureTimeEmergencyCareClinicalInvestigation(
          DateTimeFormatter.formatTime(performed, TimeType.type));

    }
    return careInvestigation;
  }

}
