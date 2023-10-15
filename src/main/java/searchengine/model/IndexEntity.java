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
@Table(name = "`index`")
@NoArgsConstructor

public class IndexEntity {
    @Setter(value = AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "page_id", nullable = false, referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_index_page"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PageEntity page;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "lemma_id", nullable = false, referencedColumnName = "id"
            , foreignKey = @ForeignKey(name = "fk_index_lemma"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LemmaEntity lemma;

    @Column(name = "rank", columnDefinition = "FLOAT NOT NULL")
    private float rank;

    public IndexEntity(PageEntity page, LemmaEntity lemma, float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }
}