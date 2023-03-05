package typeGenerator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SampleSubModel<T> {

    @JsonIgnore
    private String ignore;

    private String string;

    private int primitiveInt;

    private Integer wrapperInteger;

    private boolean primitiveBoolean;

    private Boolean wrapperBoolean;

    private List<T> listGenerics;

}
