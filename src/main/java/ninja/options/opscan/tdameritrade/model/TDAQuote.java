package ninja.options.opscan.tdameritrade.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TDAQuote {

    @JsonProperty("askPrice")
    private double ask;
    private int askSize;
    @JsonProperty("bidPrice")
    private double bid;
    private int bidSize;
    @JsonProperty("netChange")
    private double change;
    @JsonProperty("closePrice")
    private double close;
    private boolean delayed;
    private String description;
    private String exchangeName;
    @JsonProperty("52WkHigh")
    private double fiftyTwoWeekHigh;
    @JsonProperty("52WkLow")
    private double fiftyTwoWeekLow;
    private double highPrice;
    @JsonProperty("regularMarketLastPrice")
    private double last;
    private double lowPrice;
    private double mark;
    @JsonProperty("markChangeInDouble")
    private double markChange;
    @JsonProperty("markPercentChangeInDouble")
    private double markPercentChange;
    private double openPrice;
    @JsonProperty("netPercentChangeInDouble")
    private double percentChange;
    @JsonProperty("quoteTimeInLong")
    private long quoteTime;
    private String symbol;
    private long totalVolume;
    @JsonProperty("tradeTimeInLong")
    private long tradeTime;

}
