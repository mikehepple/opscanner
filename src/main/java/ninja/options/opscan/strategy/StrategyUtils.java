package ninja.options.opscan.strategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyUtils {

    static float calculateRoi(float investment, float maxProfit) {
        return (maxProfit / investment) * 100;
    }

    static float annualizedReturn(float investment, float maxProfit, int numDays) {
        return calculateRoi(investment, maxProfit) * (250f / numDays);
    }

}
