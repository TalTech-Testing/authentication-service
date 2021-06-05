package ee.taltech.arete_admin_panel.domain;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @NotNull
    private Integer id;

    @NotNull
    @Builder.Default
    private String uniid = "NaN"; // TalTech student identificator: envomp - Ago guarantee to be unique

    // last used
    private String gitRepo;

    @NotNull
    private Long firstTested;

    @NotNull
    private Long lastTested;

    @NotNull
    @Builder.Default
    private Set<Long> timestamps = new HashSet<>();

    @NotNull
    @Builder.Default
    private Set<String> courses = new HashSet<>();

    @NotNull
    @Builder.Default
    @ElementCollection
    private Set<String> slugs = new HashSet<>();

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
    private Integer differentSlugs = 0;

    @NotNull
    @Builder.Default
    private Integer differentCourses = 0;

    @NotNull
    @Builder.Default
    private Integer commitsStyleOK = 0;

}
