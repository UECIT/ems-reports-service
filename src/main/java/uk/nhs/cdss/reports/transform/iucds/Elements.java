package uk.nhs.cdss.reports.transform.iucds;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import uk.nhs.connect.iucds.cda.ucr.II;

@UtilityClass
public class Elements {

  /**
   * Creates a new {@link II} ID element with the specified root code system
   *
   * @param addNewId           method to create the ID element in the required parent
   * @param system             OID code of the ID system/type
   * @param value              the ID value
   * @param assigningAuthority the issuer of the ID - may be null
   * @return
   */
  public II addId(@NotNull Supplier<II> addNewId,
      @NotNull String system,
      @NotNull String value,
      @Nullable String assigningAuthority) {
    II id = addNewId.get();
    id.setRoot(system);
    id.setExtension(value);
    if (assigningAuthority != null) {
      id.setAssigningAuthorityName(assigningAuthority);
    }
    return id;
  }

  /**
   * @see #addId(Supplier, String, String, String)
   */
  public II addId(@NotNull Supplier<II> addNewId,
      @NotNull String system,
      @NotNull String value) {
    return addId(addNewId, system, value, null);
  }
}
