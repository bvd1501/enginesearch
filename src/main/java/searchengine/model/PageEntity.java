package searchengine.model;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import searchengine.config.BadLinks;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "page"
        , uniqueConstraints = {
                    @UniqueConstraint(name = "idx_path_site", columnNames = {"site_id", "path"})}
        , indexes = {@Index(name = "idx_path", columnList = "path")
                    , @Index(name = "idx_site_id", columnList = "site_id")}
)
@Slf4j
public class PageEntity {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "site_id", nullable = false, referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_page_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteEntity site;

    @Column(name = "path", columnDefinition = "TEXT NOT NULL")
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", length = 16777215, nullable = false, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;

    public PageEntity(SiteEntity site, String fullPath) {
        this.site = site;
        this.path = "";
        this.code = 0;
        this.content = "";
        URI baseURL = URI.create(site.getUrl());
        boolean isPathBad = (!fullPath.startsWith(site.getUrl())) ||
                (EnumSet.allOf(BadLinks.class).stream().anyMatch(enumElement ->
                        fullPath.contains(enumElement.toString())));
        if (!isPathBad) {
            try {
                URI dirtyUri = URI.create(fullPath);//
                URI cleanURI = new URI(dirtyUri.getScheme(), dirtyUri.getRawAuthority(),
                        dirtyUri.getRawPath(), null, null);
                this.path = "/" + baseURL.relativize(cleanURI);
            } catch (IllegalArgumentException | URISyntaxException e) {
                log.error("Bad link: " + e.getMessage());
            }
        }
    }

   public String getFullPath() {
       URI baseURI = URI.create(site.getUrl());
       return baseURI.toString().replaceFirst("/$","")+path;
   }


}
