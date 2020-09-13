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
@Table(name = "course_student")
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Student student;

    @NotNull
    @Builder.Default
    private String uniid = "NaN";

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Course course;

    @NotNull
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> timestamps = new HashSet<>();

    @NotNull
    @Builder.Default
    @ElementCollection
    private Set<String> slugs = new HashSet<>();

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

    @Builder.Default
    @ElementCollection
    private Set<CodeError> diagnosticCodeErrors = new HashSet<>();

    @NotNull
    @Builder.Default
    private Integer totalTestErrors = 0;

    @Builder.Default
    @ElementCollection
    private Set<CodeError> testCodeErrors = new HashSet<>();

    @NotNull
    @Builder.Default
    private Integer differentSlugs = 0;

    @NotNull
    @Builder.Default
    private Integer commitsStyleOK = 0;

}