package ninja.options.opscan.results;

import lombok.Getter;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.strategy.Strategy;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.sorting.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScanResultTable {

    @Getter
    private Table table;
    private TableSettings tableSettings;

    public ScanResultTable(List<ScanResult> results, TableSettings tableSettings) {
        this.tableSettings = tableSettings;

        Map<String, Column<?>> columnMap = new HashMap<>(results.size());
        List<String> defaultColumnOrder = new ArrayList<>();

        results.stream()
                .map(ScanResult::strategy)
                .map(Strategy::toColumns)
                .forEach(cl -> {
                    cl.forEach(c -> {
                        if (!columnMap.containsKey(c.name())) {
                            columnMap.put(c.name(), c);
                            defaultColumnOrder.add(c.name());
                        } else {
                            columnMap.get(c.name()).append((Column) c);
                        }
                    });
                });

        this.table = Table.create(tableSettings.name(), defaultColumnOrder
                .stream()
                .map(columnMap::get)
                .collect(Collectors.toList())
        );

        if (!results.isEmpty()) {
            configureSorting(results.get(0).strategy().defaultSort());
        }

    }

    private void configureSorting(Sort defaultSort) {
        if (tableSettings.hasSortBy()) {
            this.table = this.table.sortOn(Sort.on(tableSettings.sortBy(),
                    tableSettings.descending() ? Sort.Order.DESCEND : Sort.Order.ASCEND));
        } else {
            this.table = this.table.sortOn(defaultSort);
        }
    }

}
