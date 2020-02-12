package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
@Builder
@Table(name = "slug_student")
@NoArgsConstructor
@AllArgsConstructor
public class SlugStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(cascade = {CascadeType.ALL})
    private Student student;

    @NotNull
    @Builder.Default
    private String uniid = "NaN";

    @JsonIgnore
    @NotNull
    @ManyToOne(cascade = {CascadeType.ALL})
    private Slug slug;

    @NotNull
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> timestamps = new HashSet<>();

    @NotNull
    @Builder.Default
    private Double highestPercent = 0.0;

    @NotNull
    @Builder.Default
    private Long latestSubmission = 0L;

    @NotNull
    @Builder.Default
    private Integer totalCommits = 0;

    @NotNull
    @Builder.Default
    private Integer totalTestsRan = 0;

    @NotNull
    @Builder.Default
    private Integer totalTestsPassed = 0;

    @NotNull
    @Builder.Default
    private Integer totalDiagnosticErrors = 0;

    @NotNull
    @Builder.Default
    private Integer totalTestErrors = 0;

    @NotNull
    @Builder.Default
    private Integer failedCommits = 0;

    @NotNull
    @Builder.Default
    private Integer commitsStyleOK = 0;

}