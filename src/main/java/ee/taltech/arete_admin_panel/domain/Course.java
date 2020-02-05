package ee.taltech.arete_admin_panel.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@ToString
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String gitUrl;

    @NotNull
    private String name; // Every git url has a namespace

    @NotNull
    @Builder.Default
    @OneToMany(cascade = {CascadeType.MERGE})
    private Set<Slug> slugs = new HashSet<>(); // only count unique ones


}
