package searchengine.model;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor

//@Table(name = "page", uniqueConstraints = {
//        @UniqueConstraint(name = "idx_page_site", columnNames = {"site_id", "path"})
//})
@Table(name = "page")
public class PageEntity {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "fk_site_id",
            value = ConstraintMode.CONSTRAINT,
            foreignKeyDefinition = "FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE"))
//    @JoinColumn (name = "site_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    @BatchSize(size = 10)
    private SiteEntity site;

    @Column(name = "path", columnDefinition = "TEXT NOT NULL, Index (path(1024))")
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    public PageEntity(SiteEntity site, String path, Integer code, String content) {
        this.site = site;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    public PageEntity(SiteEntity site, String path) {
        this.site = site;
        this.path = path;
        this.code = 0;
        this.content = "";
    }
}
