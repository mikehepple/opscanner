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
public class LongVerticalSpread extends AbstractVerticalSpread {

    @Getter
    private final TDAOption longPosition;
    @Getter
    private final TDAOption shortPosition;



    @Override
    public boolean valid() {
        return premium() > 0f && width() > 0f;
    }

    @Override
    public Sort defaultSort() {
        return Sort.on("ratio", Sort.Order.ASCEND);
    }

    public float maxLoss() {
        return premium();
    }

    public float maxProfit() {
        return width() - premium();
    }

    @Override
    public String description() {

        String strikes = String.format("%5s / %-5s",
                formatCurrency(shortPosition.getStrikePrice()),
                formatCurrency(longPosition.getStrikePrice())
        );

        return String.format("%4s %15s %15s (%-3d) @ $%.2f - Width %-5s - Ratio %.2f - Max Profit $%.2f, Max Loss $%.2f - Delta %.2f/%.2f",
                longPosition.getPutCall().name(),
                strikes,
                expiryToString(longToDate(longPosition.getExpirationDate())),
                Math.round(longPosition.getDaysToExpiration()),
                premium(),
                formatCurrency(width()),
                premiumWidthRatio(),
                formatCurrency(maxProfit()),
                formatCurrency(maxLoss()),
                longPosition.getDelta(),
                shortPosition.getDelta()
        );
    }
}
