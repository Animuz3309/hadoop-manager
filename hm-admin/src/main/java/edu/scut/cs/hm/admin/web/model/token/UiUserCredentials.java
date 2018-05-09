package edu.scut.cs.hm.admin.web.model.token;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class UiUserCredentials {
    @NotNull
    private final String username;
    @NotNull @Length(min = 3)
    private final String password;
}
