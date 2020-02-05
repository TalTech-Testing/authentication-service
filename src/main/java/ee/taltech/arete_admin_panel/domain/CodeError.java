package ee.taltech.arete_admin_panel.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Embeddable;

@Getter
@Setter
@Builder
@ToString
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