package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String testingPlatform;

    private String hash; // references to job.

    private String uniid; // gitlab namespace: envomp

    private String root; // gitlab path for student: iti0102-2019

    private String gitStudentRepo;

    private String gitTestSource;

    private Long timestamp;

}
