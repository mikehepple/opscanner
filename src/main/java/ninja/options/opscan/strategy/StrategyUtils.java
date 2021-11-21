package ninja.options.opscan.strategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyUtils {

    static DateTimeFormatter expiryFormatterUnder12Mo = DateTimeFormatter.ofPattern("d MMM");
    static DateTimeFormatter expiryFormatterOver12Mo = DateTimeFormatter.ofPattern("d MMM yyyy");

    static float calculateRoi(float investment, float maxProfit) {
        return (maxProfit / investment) * 100;
    }

    static float annualizedReturn(float investment, float maxProfit, int numDays) {
        return calculateRoi(investment, maxProfit) * (250f / numDays);
    }

    static LocalDate longToDate(long timestamp) {
        var zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
        return zdt.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDate();
    }

    static String expiryToString(LocalDate expiry) {
        if (expiry.isAfter(LocalDate.now().plusYears(1))) {
            return expiryFormatterOver12Mo.format(expiry);
        }
        return expiryFormatterUnder12Mo.format(expiry);
    }

    static String formatCurrency(float input) {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        if (Math.round(input) == input) {
            nf.setMinimumFractionDigits(0);
        }
        return nf.format(input);
    }

}
