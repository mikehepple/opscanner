package ninja.options.opscan.strategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ninja.options.opscan.tdameritrade.model.TDAPutCall;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.FloatColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.columns.numbers.NumberColumnFormatter;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Columns {

    static StringColumn stringCol(String name, String value) {
        return StringColumn.create(name, value);
    }

    static StringColumn putCallCol(String name, TDAPutCall putCall) {
        return switch (putCall) {
            case CALL -> stringCol(name, "Call");
            case PUT -> stringCol(name, "Put");
        };
    }

    static IntColumn intCol(String name, int value) {
        return IntColumn.create(name, new Integer[]{value});
    }

    static FloatColumn floatCol(String name, float value) {
        return floatCol(name, value, NumberColumnFormatter.fixedWithGrouping(2));
    }

    static FloatColumn floatCol(String name, float value, NumberColumnFormatter formatter) {
        FloatColumn col = FloatColumn.create(name, value);
        col.setPrintFormatter(formatter);
        return col;
    }

    static DateColumn dateCol(String name, LocalDate date) {
        return DateColumn.create(name, date);
    }

    static DateColumn dateCol(String name, long date) {
        var zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("UTC"));
        return DateColumn.create(name, zdt.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDate());
    }

    static NumberColumnFormatter usdFormatter() {
        return NumberColumnFormatter.currency("en-US", "US");
    }

    static NumberColumnFormatter percentFormatter() {
        return percentFormatter(2);
    }

    static NumberColumnFormatter percentFormatter(int fractionalDigits) {
        return NumberColumnFormatter.percent(fractionalDigits);
    }

    static class PremiumFormatter extends NumberFormat {

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            return null;
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            return null;
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            return null;
        }
    }


}
