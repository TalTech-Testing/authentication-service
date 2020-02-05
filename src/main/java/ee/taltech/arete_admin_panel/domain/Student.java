package ee.taltech.arete_admin_panel.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "students")
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String uniid; // TalTech student identificator: envomp - Ago guarantee to be unique

    @NotNull
    @OneToMany(cascade = {CascadeType.MERGE})
    @Builder.Default
    private Set<Course> courses = new HashSet<>(); // only count unique ones

}
