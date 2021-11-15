package ninja.options.opscan.results;

public record TableSettings(
        String sortBy,
        boolean descending,
        String name
) {

    boolean hasSortBy() {
        return this.sortBy != null && !"".equals(this.sortBy);
    }

}
