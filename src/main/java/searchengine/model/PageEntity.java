package searchengine.model;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

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

public class PageEntity {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "site_id", nullable = false, referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_page_site"))
    @BatchSize(size = 10)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteEntity site;

    @Column(name = "path", columnDefinition = "TEXT NOT NULL")
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", length = 16777215, nullable = false, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;

    public PageEntity(SiteEntity site, String path) {
        this.site = site;
        this.path = path;
        this.code = 0;
        this.content = " ";
    }

    public PageEntity(SiteEntity site, String path, Integer code, String content) {
        this.site = site;
        this.path = path;
        this.code = code;
        this.content = content;
    }


}
