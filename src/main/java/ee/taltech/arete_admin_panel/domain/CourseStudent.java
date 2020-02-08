package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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

    @NotNull
    @ManyToOne(cascade = {CascadeType.ALL})
    private Student student;

    @JsonIgnore
    @NotNull
    @ManyToOne(cascade = {CascadeType.ALL})
    private Course course;

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