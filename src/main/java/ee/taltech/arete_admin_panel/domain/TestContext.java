package ee.taltech.arete_admin_panel.domain;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "text_context")
@Entity
@JsonClassDescription("Test results from test file")
public class TestContext {


    @OneToMany(cascade = {CascadeType.ALL})
    @JsonPropertyDescription("List of unittests tested")
    List<UnitTest> unitTests;

    @Column(length = 1023)
    @JsonPropertyDescription("Test name")
    String name;

    @Column(length = 1023)
    @JsonPropertyDescription("Test file path")
    String file;

    @JsonPropertyDescription("Test start time")
    Long startDate;

    @JsonPropertyDescription("Test end time")
    Long endDate;

//	String mode;
//	String welcomeMessage;
//	Integer identifier;
//	Integer count;

    @JsonPropertyDescription("Sum of test weights")
    Integer weight;

    @JsonPropertyDescription("Number of passed tests")
    Integer passedCount;

    @JsonPropertyDescription("Total grade for this test file")
    Double grade;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
}

