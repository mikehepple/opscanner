package ninja.options.opscan.strategy;

import ninja.options.opscan.tdameritrade.model.TDAOption;
import tech.tablesaw.columns.Column;

import java.util.List;

import static ninja.options.opscan.strategy.Columns.*;

public abstract class AbstractVerticalSpread implements Strategy {

    public abstract TDAOption getLongPosition();
    public abstract TDAOption getShortPosition();

    public float premium() {
        float debit = this.getLongPosition().getAsk();
        float credit = this.getShortPosition().getBid();
        return debit - credit;
    }

    public float width() {
        var w = this.getLongPosition().getStrikePrice() - this.getShortPosition().getStrikePrice();
        return Math.max(w, w * -1);
    }

    public float premiumWidthRatio() {
        return Math.max(premium(), premium()*-1) / width();
    }

    @Override
    public List<TDAOption> longLegs() {
        return List.of(getLongPosition());
    }

    @Override
    public List<TDAOption> shortLegs() {
        return List.of(getShortPosition());
    }

    @Override
    public List<Column<?>> toColumns() {
        return List.of(
                dateCol("expiry", this.getLongPosition().getExpirationDate()),
                floatCol("premium", premium(), usdFormatter()),
                floatCol("width", width()),
                floatCol("ratio", premiumWidthRatio()),
                putCallCol("type", this.getLongPosition().getPutCall()),
                floatCol("long_strike", this.getLongPosition().getStrikePrice()),
                floatCol("long_ask", this.getLongPosition().getAsk(), usdFormatter()),
                floatCol("long_delta", this.getLongPosition().getDelta()),
                floatCol("long_spread", this.getLongPosition().getAsk() - this.getLongPosition().getBid()),
                intCol("long_oi", this.getLongPosition().getOpenInterest()),
                intCol("long_vol", this.getLongPosition().getTotalVolume()),
                floatCol("short_strike", this.getShortPosition().getStrikePrice()),
                floatCol("short_ask", this.getShortPosition().getAsk(), usdFormatter()),
                floatCol("short_delta", this.getShortPosition().getDelta()),
                intCol("short_oi", this.getShortPosition().getOpenInterest()),
                intCol("short_vol", this.getShortPosition().getTotalVolume()),
                floatCol("short_spread", this.getShortPosition().getAsk() - this.getShortPosition().getBid())
        );
    }

}
