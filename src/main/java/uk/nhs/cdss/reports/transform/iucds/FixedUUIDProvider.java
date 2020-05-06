package uk.nhs.cdss.reports.transform.iucds;

import java.nio.ByteBuffer;
import java.util.UUID;

public class FixedUUIDProvider implements UUIDProvider {

  ByteBuffer seed = ByteBuffer.allocate(8);

  public FixedUUIDProvider() {
  }

  public FixedUUIDProvider(long seed) {
    this.seed.putLong(0, seed);
  }

  @Override
  public String get() {
    return UUID.nameUUIDFromBytes(seed.array()).toString().toUpperCase();
  }
}
