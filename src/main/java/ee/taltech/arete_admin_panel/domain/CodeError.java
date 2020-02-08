package ee.taltech.arete_admin_panel.domain;

import lombok.*;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CodeError {

    @NotNull
    private String errorType;

    @NotNull
    @Builder.Default
    private Integer repetitions = 0;
}