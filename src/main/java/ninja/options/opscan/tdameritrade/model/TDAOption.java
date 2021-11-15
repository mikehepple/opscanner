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
public class TDAOption {

    private TDAPutCall putCall;
    private String status;
    private String symbol;
    private String description;
    private String exchangeName;
    private float bid;
    private float ask;
    private float mark;
    private float last;
    private String bidAskSize;
    private int bidSize;
    private int askSize;
    private int lastSize;
    private float highPrice;
    private float lowPrice;
    private float openPrice;
    private float closePrice;
    private int totalVolume;
    private long quoteTimeInLong;
    private long tradeTimeInLong;
    private double netChange;
    private float volatility;
    private float delta;
    private float gamma;
    private float theta;
    private float vega;
    private float rho;
    private float timeValue;
    private boolean isInTheMoney;
    private float theoreticalOptionValue;
    private float theoreticalVolatility;
    private boolean isMini;
    private boolean isNonStandard;
    private float strikePrice;
    private long expirationDate;
    private String expirationType;
    private float multiplier;
    private String settlementType;
    private String deliverableNote;
    @JsonProperty("isIndexOption")
    private boolean indexOption;
    private float percentChange;
    private float markChange;
    private float markPercentChange;
    private int openInterest;
    private float daysToExpiration;
    private long lastTradingDay;

}
