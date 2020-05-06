package uk.nhs.cdss.reports.transform.iucds;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Coding;
import uk.nhs.connect.iucds.cda.ucr.CD;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.CS;
import uk.nhs.connect.iucds.cda.ucr.CS.Factory;
import uk.nhs.connect.iucds.cda.ucr.II;

@UtilityClass
public class Elements {

  /**
   * Creates a new {@link II} ID element with the specified root code system
   *
   * @param addNewId           method to create the ID element in the required parent
   * @param root               OID code of the ID system/type
   * @param extension          the ID value
   * @param assigningAuthority the issuer of the ID - may be null
   * @return
   */
  public II addId(@NotNull Supplier<II> addNewId,
      @NotNull String root,
      @NotNull String extension,
      @Nullable String assigningAuthority) {
    II id = addNewId.get();
    id.setRoot(root);
    if (extension != null) {
      id.setExtension(extension);
    }
    if (assigningAuthority != null) {
      id.setAssigningAuthorityName(assigningAuthority);
    }
    return id;
  }

  /**
   * @see #addId(Supplier, String, String, String)
   */
  public II addId(@NotNull Supplier<II> addNewId,
      @NotNull String root,
      @NotNull String extension) {
    return addId(addNewId, root, extension, null);
  }

  /**
   * @see #addId(Supplier, String, String, String)
   */
  public II addId(@NotNull Supplier<II> addNewId,
      @NotNull String root) {
    return addId(addNewId, root, null, null);
  }

  /**
   * Creates a new {@link CS} element and applies it using the given consumer
   *
   * @param system
   * @param code
   * @param consumer
   * @return the created element
   */
  public CS setStatusCode(String system, String code, Consumer<CS> consumer) {
    CS statusCode = Factory.newInstance();
    statusCode.setCodeSystem(system);
    statusCode.setCode(code);
    consumer.accept(statusCode);
    return statusCode;
  }

  public CE addCode(Supplier<CE> supplier, String system, String code, String display) {
    CE ce = supplier.get();
    ce.setCodeSystem(system);
    ce.setCode(code);
    if (StringUtils.isNotEmpty(display)) {
      ce.setDisplayName(display);
    }
    return ce;
  }

  public static CE addCode(Supplier<CE> supplier, Coding coding) {
    return addCode(supplier, coding.getSystem(), coding.getCode(), coding.getDisplay());
  }

  /**
   * Creates a new {@link CD} element using the given supplier and populates it with the code and
   * system
   *
   * @param supplier
   * @param system
   * @param code
   * @return the created element
   */
  public static CD addConcept(Supplier<CD> supplier, String system, String code) {
    CD concept = supplier.get();
    concept.setCodeSystem(system);
    concept.setCode(code);
    return concept;
  }
}
