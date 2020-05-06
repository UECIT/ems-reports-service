package uk.nhs.cdss.reports.transform.ecds;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Identifiers {

  @Value("${ecds.sender_identity:1100000000}")
  private String senderIdentity;
  @Value("${ecds.sender_ods_organisation:8HW00}")
  private String senderOdsOrganisation;
  @Value("${ecds.receiver_identity:1100000000}")
  private String receiverIdentity;
}
