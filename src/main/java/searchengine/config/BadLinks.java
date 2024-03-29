package searchengine.config;

import lombok.Getter;


@Getter
public enum BadLinks {
    SPACE(" "),
    LATTICE("#"),
    DOWNLOAD("download"),
    QUOTES ("\""),
    SQUARE_BRACKET("["),
    PARENTHESIS ("{"),
    PPT (".ppt"),
    PDF (".pdf"),
    DOC (".doc"),
    JPG (".jpg"),
    JPEG (".jpeg"),
    PNG (".png"),
    //WEBP (".webp"),
    XLS (".xls"),
    //NC (".nc"),
    ZIP (".zip");

    private final String title;

    BadLinks (String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
