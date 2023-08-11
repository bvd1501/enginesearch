package searchengine.config;

import lombok.Getter;


@Getter
public enum BadLinks {
    //SPACE1(" "),
    //SPACE2("%20"),
    //LATTICE("#"),
    DOWNLOAD("download"),
    PDF(".pdf"),
    JPG(".jpg"),
    JPEG(".jpeg"),
    PNG(".png"),
    SVG(".svg"),
    DOC(".doc"),
    HTTP ("%20http"),
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
