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
@Table(name = "lemma"
        , uniqueConstraints = {@UniqueConstraint(name = "idx_lemma_site", columnNames = {"site_id", "lemma"})}
        , indexes = {@Index(name = "idx_lemma", columnList = "lemma")}
)

public class LemmaEntity {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "fkl_site_id",
//            value = ConstraintMode.CONSTRAINT,
//            foreignKeyDefinition = "FOREIGN KEY (site_id) REFERENCES site(id) ON DELETE CASCADE"))
    @JoinColumn (name = "site_id", nullable = false, referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_lemma_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @BatchSize(size = 10)
    private SiteEntity site;

    @Column(name = "lemma", columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;

    @Column(name = "frequency", nullable = false, columnDefinition = "INT")
    private Integer frequency;

    public LemmaEntity(SiteEntity site, String lemma) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = 1;
    }
}
