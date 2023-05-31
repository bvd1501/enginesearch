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
@Table(name = "index_entity")
@NoArgsConstructor

public class IndexEntity {
    @Setter(value = AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false, foreignKey = @ForeignKey(name = "fk_page_id",
            value = ConstraintMode.CONSTRAINT,
            foreignKeyDefinition = "FOREIGN KEY (page_id) REFERENCES page(id) ON DELETE CASCADE"))
    @BatchSize(size = 10)
    private PageEntity page;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lemma_id",
            value = ConstraintMode.CONSTRAINT,
            foreignKeyDefinition = "FOREIGN KEY (lemma_id) REFERENCES lemma(id) ON DELETE CASCADE"))
    @BatchSize(size = 10)
    private LemmaEntity lemma;

    @Column(name = "rank", columnDefinition = "FLOAT NOT NULL")
    private float rank;

    public IndexEntity(PageEntity page, LemmaEntity lemma) {
        this.page = page;
        this.lemma = lemma;
        this.rank = 0;
    }
}