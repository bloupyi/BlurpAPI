package fr.bloup.blurpapi.database;

import lombok.Getter;

import java.util.EnumSet;

@Getter
public class Column {
    private final String name;
    private final Class<?> type;
    private final EnumSet<ColumnOption> options;

    public Column(String name, Class<?> type, ColumnOption... options) {
        this.name = name;
        this.type = type;
        this.options = options.length == 0 ? EnumSet.noneOf(ColumnOption.class) : EnumSet.of(options[0], options);
    }

}
