package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String testingPlatform;

    private String returnUrl;

    private String gitStudentRepo;
    //  or
    @Column(columnDefinition = "TEXT")
    private String source;

    private String gitTestSource;
    // or
    @Column(columnDefinition = "TEXT")
    private String testSource;

    private String hash;

    private String uniid; // gitlab namespace: envomp

    private String course; // gitlab namespace with path for tester: iti0102-2019/ex

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

    private Integer analyzed = 0;

}
