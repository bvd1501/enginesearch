package searchengine.model;



import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "site")
public class SiteEntity {
    @Setter(value = AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private Date statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String last_error;

    @Column(name="url", nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name="name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

//   @OneToMany(fetch = FetchType.LAZY, mappedBy = "site", cascade = CascadeType.ALL)
//   private Set<PageEntity> pages = new HashSet<>();
//
//   @OneToMany(fetch = FetchType.LAZY, mappedBy = "site", cascade = CascadeType.ALL)
//   private Set<LemmaEntity> lemmas;

    public SiteEntity(String url, String name) {
        this.url = url;
        this.name = name;
        this.last_error = null;
        this.status = StatusType.FAILED;
        this.statusTime = new java.util.Date();
    }
}
