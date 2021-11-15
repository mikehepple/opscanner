package ninja.options.opscan.tdameritrade.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TDAOptionChainUnderlying {
    private double ask;
    private int askSize;
    private double bid;
    private int bidSize;
    private double change;
    private double close;
    private boolean delayed;
    private String description;
    private String exchangeName;
    private double fiftyTwoWeekHigh;
    private double fiftyTwoWeekLow;
    private double highPrice;
    private double last;
    private double lowPrice;
    private double mark;
    private double markChange;
    private double markPercentChange;
    private double openPrice;
    private double percentChange;
    private long quoteTime;
    private String symbol;
    private long totalVolume;
    private long tradeTime;
}