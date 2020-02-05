package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String testingPlatform;

    private String gitStudentRepo;
    //  or
    @Column(columnDefinition = "TEXT")
    private String source;

    private String gitTestRepo;

    private String hash;

    private String uniid; // gitlab namespace: envomp

    private String root; // gitlab namespace with path for tester: iti0102-2019

    private String slug;

    private String commitMessage;

    @ElementCollection
    @CollectionTable(name = "docker_extra", joinColumns = @JoinColumn(name = "id"))
    private Set<String> dockerExtra;

    @ElementCollection
    @CollectionTable(name = "system_extra", joinColumns = @JoinColumn(name = "id"))
    private Set<String> systemExtra;

    private Integer dockerTimeout;

    private Long timestamp;

    private Integer priority;

    private Boolean failed;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String consoleOutput;

    @NotNull
    @Builder.Default
    private Integer analyzed = 0;

}
