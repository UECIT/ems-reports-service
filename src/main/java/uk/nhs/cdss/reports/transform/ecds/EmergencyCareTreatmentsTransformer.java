package uk.nhs.cdss.reports.transform.ecds;

import static org.apache.commons.lang3.time.DateUtils.toCalendar;

import java.util.Calendar;
import org.hl7.fhir.dstu3.model.Procedure;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.reports.model.EncounterReportInput;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.EmergencyCareTreatmentsSnomedCt;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;

@Component
public class EmergencyCareTreatmentsTransformer {

  public EmergencyCareTreatmentsSnomedCt[] transform(EncounterReportInput input) {
    if (input.getProcedures() == null) {
      return null;
    }

    return input.getProcedures().stream()
        .map(this::transform)
        .toArray(EmergencyCareTreatmentsSnomedCt[]::new);
  }

  private EmergencyCareTreatmentsSnomedCt transform(Procedure procedure) {
    EmergencyCareTreatmentsSnomedCt careTreatment = EmergencyCareTreatmentsSnomedCt.Factory.newInstance();

    careTreatment.setEmergencyCareProcedureSnomedCt(procedure.getCode().getCodingFirstRep().getCode());

    if (procedure.hasPerformedDateTimeType()) {
      Calendar performed = toCalendar(procedure.getPerformedDateTimeType().getValue());

      careTreatment.xsetProcedureDateEmergencyCareProcedure(
          DateTimeFormatter.formatDate(performed, DateType.type));
      careTreatment.xsetProcedureTimeEmergencyCareProcedure(
          DateTimeFormatter.formatTime(performed, TimeType.type));

    }
    return careTreatment;
  }

}
