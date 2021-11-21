package ninja.options.opscan.strategy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAPutCall;
import tech.tablesaw.columns.Column;
import tech.tablesaw.sorting.Sort;

import java.text.NumberFormat;
import java.util.List;

import static ninja.options.opscan.strategy.Columns.*;
import static ninja.options.opscan.strategy.StrategyUtils.*;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortPut implements Strategy {

    @Getter
    private final TDAOption shortPosition;

    @Override
    public float premium() {
        return shortPosition.getBid() * -1;
    }

    @Override
    public boolean valid() {
        return shortPosition.getPutCall() == TDAPutCall.PUT;
    }

    @Override
    public List<TDAOption> longLegs() {
        return List.of();
    }

    @Override
    public List<TDAOption> shortLegs() {
        return List.of(shortPosition);
    }

    public float roi() {
        return calculateRoi(this.getShortPosition().getStrikePrice() * 100, premium() * -1);
    }

    public float annualizedRoi() {
        return annualizedReturn(this.getShortPosition().getStrikePrice() * 100, premium() * -1,
                Math.round(this.shortPosition.getDaysToExpiration()));
    }

    @Override
    public List<Column<?>> toColumns() {
        return List.of(
                dateCol("expiry", this.getShortPosition().getExpirationDate()),
                floatCol("strike", this.getShortPosition().getStrikePrice()),
                floatCol("bid", this.getShortPosition().getBid(), usdFormatter()),
                floatCol("ask", this.getShortPosition().getAsk(), usdFormatter()),
                floatCol("spread", this.getShortPosition().getAsk() - this.getShortPosition().getBid()),
                floatCol("delta", this.getShortPosition().getDelta()),
                intCol("oi", this.getShortPosition().getOpenInterest()),
                intCol("vol", this.getShortPosition().getTotalVolume()),
                floatCol("roi",
                        roi(),
                        percentFormatter()),
                floatCol("roi_annual", annualizedRoi(), percentFormatter())
        );
    }

    @Override
    public Sort defaultSort() {
        return Sort.on("roi_annual", Sort.Order.DESCEND);
    }

    @Override
    public String description() {
        return String.format("%10s %10s (%-3dd) @ $%.2f - ROI %3s (%3s annualized)",
                NumberFormat.getCurrencyInstance().format(shortPosition.getStrikePrice()),
                expiryToString(longToDate(shortPosition.getExpirationDate())),
                Math.round(shortPosition.getDaysToExpiration()),
                premium(),
                NumberFormat.getPercentInstance().format(roi()),
                NumberFormat.getPercentInstance().format(annualizedRoi())
        );
    }

}
