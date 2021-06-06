package ee.taltech.arete_admin_panel.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String testingPlatform;

    private String gitStudentRepo;

    private String gitTestRepo;

    @NotNull
    private String hash;

    @NotNull
    @Builder.Default
    private String uniid = "NaN"; // gitlab namespace: envomp

    private String email;

    private String type;

    private String version;

    @NotNull
    private String root; // gitlab namespace with path for tester: iti0102-2019

    @NotNull
    private String slug;

    private String commitMessage;

    @OneToMany(cascade = {CascadeType.ALL})
    private List<TestContext> testSuites;

    @OneToMany(cascade = {CascadeType.ALL})
    private List<Error> errors;

    private String dockerExtra;

    private String dockerTestRoot;

    private String dockerContentRoot;

    @ElementCollection
    @CollectionTable(name = "system_extra", joinColumns = @JoinColumn(name = "id"))
    private Set<String> systemExtra;

    private Integer dockerTimeout;

    @NotNull
    private Long timestamp;

    private Long receivedTimestamp;

    private Long finishedTimestamp;

    private Integer priority;

    private Integer totalCount;

    private Integer totalPassedCount;

    private Integer style;

    private Double totalGrade;

    private Boolean failed;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT", table = "consoleOutput")
    private String consoleOutputs;

    @NotNull
    @Builder.Default
    private Integer analyzed = 0;

}
