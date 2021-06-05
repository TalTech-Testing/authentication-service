package ee.taltech.arete_admin_panel.domain;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Slug {

    @NotNull
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private String courseUrl;

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
    private Integer differentStudents = 0;

    @NotNull
    @Builder.Default
    private Integer commitsStyleOK = 0;

}
