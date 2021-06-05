package ee.taltech.arete_admin_panel.domain;

import lombok.*;

import javax.persistence.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "error")
@Entity
public class Error {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer columnNo;

    private String fileName;

    private String hint;

    private String kind;

    private Integer lineNo;

    private String message;
}
