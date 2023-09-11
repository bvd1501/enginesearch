package searchengine.config;

import lombok.Getter;


@Getter
public enum BadLinks {
    SPACE1(" "),
    LATTICE("#"),
    DOWNLOAD("download"),
    QUOTES ("\""),
    SQUARE_BRACKET("["),
    PARENTHESIS ("{");

    private final String title;

    BadLinks (String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
