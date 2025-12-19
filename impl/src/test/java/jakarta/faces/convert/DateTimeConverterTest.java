package jakarta.faces.convert;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlOutputText;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeConverterTest extends JUnitFacesTestCaseBase {

    @Test
    public void legacyDateGetAsStringWithoutTimezone() {
        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("date");
        converter.setPattern("MM/dd/yyyy hh:mm a z");
        facesContext.setViewRoot(new UIViewRoot());
        assertEquals("01/01/2024 12:00 PM GMT", converter.getAsString(facesContext, new HtmlOutputText(), Date.from(Instant.parse("2024-01-01T12:00:00Z"))));
    }

    @Test
    public void legacyDateGetAsStringWithTimezone() {
        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("date");
        converter.setTimeZone(TimeZone.getTimeZone("MST7MDT"));
        converter.setPattern("MM/dd/yyyy hh:mm a z");
        facesContext.setViewRoot(new UIViewRoot());
        assertEquals("01/01/2024 05:00 AM MST", converter.getAsString(facesContext, new HtmlOutputText(), Date.from(Instant.parse("2024-01-01T12:00:00Z"))));
    }

    @Test
    public void zonedDateTimeGetAsStringWithoutTimezone() {
        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("zonedDateTime");
        converter.setPattern("MM/dd/yyyy hh:mm a z");
        facesContext.setViewRoot(new UIViewRoot());
        assertEquals("01/01/2024 12:00 PM GMT", converter.getAsString(facesContext, new HtmlOutputText(), ZonedDateTime.ofInstant(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC)));
    }

    @Test
    public void zonedDateTimeGetAsStringWithTimezone() {
        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("zonedDateTime");
        converter.setTimeZone(TimeZone.getTimeZone("MST7MDT"));
        converter.setPattern("MM/dd/yyyy hh:mm a z");
        facesContext.setViewRoot(new UIViewRoot());
        assertEquals("01/01/2024 05:00 AM MST", converter.getAsString(facesContext, new HtmlOutputText(), ZonedDateTime.ofInstant(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC)));
    }
}
