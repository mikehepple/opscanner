package ninja.options.opscan.strategy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import tech.tablesaw.sorting.Sort;

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
}
