package ninja.options.opscan.strategy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import tech.tablesaw.sorting.Sort;

import static ninja.options.opscan.strategy.StrategyUtils.*;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ShortVerticalSpread extends AbstractVerticalSpread {

    @Getter
    private final TDAOption longPosition;
    @Getter
    private final TDAOption shortPosition;

    @Override
    public boolean valid() {
        return premium() < 0f && width() > 0f;
    }

    @Override
    public Sort defaultSort() {
        return Sort.on("ratio", Sort.Order.DESCEND);
    }

    public float maxLoss() {
        return width() - premium();
    }

    public float maxProfit() {
        return premium();
    }

    @Override
    public String description() {
        String strikes = String.format("%5s / %-5s",
                formatCurrency(shortPosition.getStrikePrice()),
                formatCurrency(longPosition.getStrikePrice())
        );

        return String.format("%4s %15s %10s (%-3d) @ $%.2f - Width %-5s - Ratio %.2f - Max Profit %6s, Max Loss %6s - Delta %.2f/%.2f",
                shortPosition.getPutCall().name(),
                strikes,
                expiryToString(longToDate(longPosition.getExpirationDate())),
                Math.round(longPosition.getDaysToExpiration()),
                premium(),
                formatCurrency(width()),
                premiumWidthRatio(),
                formatCurrency(maxProfit()),
                formatCurrency(maxLoss()),
                shortPosition.getDelta(),
                longPosition.getDelta()
        );
    }

}
