package ee.taltech.arete_admin_panel.domain;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
    private long id;

    @NotNull
    private String testingPlatform;

    private String gitStudentRepo;
    //  or
    @Column(columnDefinition = "TEXT")
    private String source;

    @NotNull
    private String gitTestRepo;

    @NotNull
    private String hash;

    @NotNull
    @Builder.Default
    private String uniid = "NaN"; // gitlab namespace: envomp

    @NotNull
    private String root; // gitlab namespace with path for tester: iti0102-2019

    @NotNull
    private String slug;

    private String commitMessage;

    @ElementCollection
    @CollectionTable(name = "docker_extra", joinColumns = @JoinColumn(name = "id"))
    private Set<String> dockerExtra;

    @ElementCollection
    @CollectionTable(name = "system_extra", joinColumns = @JoinColumn(name = "id"))
    private Set<String> systemExtra;

    private Integer dockerTimeout;

    @NotNull
    private Long timestamp;

    private Integer priority;

    @NotNull
    private Boolean failed;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String consoleOutput;

    @NotNull
    @Builder.Default
    private Integer analyzed = 0;

}
