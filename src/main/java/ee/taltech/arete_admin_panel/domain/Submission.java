package ee.taltech.arete_admin_panel.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String testingPlatform;

    private String hash;

    private String uniid; // gitlab namespace: envomp

    private String course; // gitlab namespace with path for tester: iti0102-2019/ex

    private Long timestamp;


}
