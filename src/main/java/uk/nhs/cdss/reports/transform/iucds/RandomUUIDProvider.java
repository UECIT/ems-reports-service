package uk.nhs.cdss.reports.transform.iucds;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RandomUUIDProvider implements UUIDProvider {

  @Override
  public String get() {
    return UUID.randomUUID().toString().toUpperCase();
  }
}
