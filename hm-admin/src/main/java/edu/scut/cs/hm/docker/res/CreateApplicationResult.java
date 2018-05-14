package edu.scut.cs.hm.docker.res;

import edu.scut.cs.hm.model.application.Application;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateApplicationResult extends ServiceCallResult {

    Application application;
}
