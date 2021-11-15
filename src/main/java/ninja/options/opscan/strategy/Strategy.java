package ninja.options.opscan.strategy;

import ninja.options.opscan.tdameritrade.model.TDAOption;
import tech.tablesaw.columns.Column;
import tech.tablesaw.sorting.Sort;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

public interface Strategy {

    float premium();
    boolean valid();

    List<TDAOption> longLegs();
    List<TDAOption> shortLegs();

    // table support

    List<Column<?>> toColumns();
    Sort defaultSort();

}
