package uk.nhs.cdss.reports.transform.ecds;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Component;
import uk.nhs.nhsia.datastandards.ecds.AttendanceOccurrenceECStructure.ReferralsToOtherServices;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;

@Component
public class ReferralsToOtherServicesTransformer {

  public ReferralsToOtherServices[] transform(
      List<ReferralRequest> referralRequests) {
    if (referralRequests == null) {
      return null;
    }

    return referralRequests.stream()
        .map(this::transformReferralRequest)
        .toArray(ReferralsToOtherServices[]::new);

  }

  private ReferralsToOtherServices transformReferralRequest(ReferralRequest referralRequest) {
    checkArgument(referralRequest.hasAuthoredOn(), "Referral request must have authored on date");
    Calendar authoredOn = DateUtils.toCalendar(referralRequest.getAuthoredOn());
    ReferralsToOtherServices referral = ReferralsToOtherServices.Factory.newInstance();

    if (referralRequest.hasServiceRequested()) {
      referral.setReferredToServiceSnomedCt(referralRequest.getServiceRequestedFirstRep().getCodingFirstRep().getCode());
    }

    referral.xsetActivityServiceRequestDateEmergencyCare(
        DateTimeFormatter.formatDate(authoredOn, DateType.type));
    referral.xsetActivityServiceRequestTimeEmergencyCare(
        DateTimeFormatter.formatTime(authoredOn, TimeType.type));
    return referral;
  }

}
