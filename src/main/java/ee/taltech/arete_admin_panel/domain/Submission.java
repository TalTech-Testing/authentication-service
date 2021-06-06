package ee.taltech.arete_admin_panel.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "submission")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String testingPlatform;

    @NotNull
    private String hash; // references to job.

    @NotNull
    private String slug;

    @NotNull
    private String uniid; // gitlab namespace: envomp

    private String root; // gitlab path for student: iti0102-2019

    @NotNull
    private String gitStudentRepo;

    @NotNull
    private String gitTestSource;

    @NotNull
    private Long timestamp;

    private Integer style;

    private Integer diagnosticErrors;

    private Integer testsPassed;

    private Integer testsRan;

    @NotNull
    @Builder.Default
    private Boolean failed = false;

}
