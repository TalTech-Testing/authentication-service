package ee.taltech.arete_admin_panel.domain;

import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "student_data_slug")
@NoArgsConstructor
@AllArgsConstructor
public class StudentDataSlug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne(cascade = {CascadeType.MERGE})
    private Student student;

    @NotNull
    @ManyToOne(cascade = {CascadeType.MERGE})
    private Slug slug;

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
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<CodeError> diagnosticCodeErrors = new HashSet<>();

    @NotNull
    @Builder.Default
    private Integer totalTestErrors = 0;

    @NotNull
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<CodeError> testCodeErrors = new HashSet<>();

    @NotNull
    @Builder.Default
    private Integer failedCommits = 0;

    @NotNull
    @Builder.Default
    private Integer commitsStyleOK = 0;

    @NotNull
    @Builder.Default
    private Double highestPercentage = 0.0;

}
