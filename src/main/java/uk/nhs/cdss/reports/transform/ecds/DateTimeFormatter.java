package uk.nhs.cdss.reports.transform.ecds;

import java.util.Calendar;
import java.util.Date;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.xmlbeans.SchemaType;
import uk.nhs.nhsia.datastandards.ecds.DateType;
import uk.nhs.nhsia.datastandards.ecds.TimeType;

@SuppressWarnings("ALL")
@UtilityClass
public class DateTimeFormatter {

  private final String TIME_FORMAT = "HH:mm:ss";
  private final String DATE_FORMAT = "yyyy-MM-dd";

  public <T extends DateType> T formatDate(Calendar value, SchemaType type) {
    String dateString = DateFormatUtils.format(value, DATE_FORMAT);
    return (T) type.newValue(dateString);
  }

  public <T extends DateType> T formatDate(Date value, SchemaType type) {
    String dateString = DateFormatUtils.format(value, DATE_FORMAT);
    return (T) type.newValue(dateString);
  }

  public <T extends TimeType> T formatTime(Calendar value, SchemaType type) {
    String timeString = DateFormatUtils.format(value, TIME_FORMAT);
    return (T) type.newValue(timeString);
  }

  public <T extends TimeType> T formatTime(Date value, SchemaType type) {
    String timeString = DateFormatUtils.format(value, TIME_FORMAT);
    return (T) type.newValue(timeString);
  }

}
