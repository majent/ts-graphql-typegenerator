package typeGenerator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SampleModel<T> implements SampleInterface {

    @JsonIgnore
    private String ignore;

    private String nonnull;

    private String string;

    private int primitiveInt;

    private Integer wrapperInteger;

    private long primitiveLong;

    private Long wrapperLong;

    private short primitiveShort;

    private Short wrapperShort;

    private double primitiveDouble;

    private Double wrapperDouble;

    private float primitiveFloat;

    private Float wrapperFloat;

    private byte primitiveByte;

    private Byte wrapperByte;

    private boolean primitiveBoolean;

    private Boolean wrapperBoolean;

    private BigDecimal bigDecimal;

    private SampleSubModel<String> subModel;

    private String[] arrayString;

    private List<T> listGenerics;

    private List<SampleSubModel<T>> listModel;

    private List<String> listString;

    private List<List<String>> listListString;

    private Map<String, Integer> map;

    private Map<SampleEnum, Integer> mapKeyEnumerate;

    private Map<String, SampleEnum> mapValueEnumerate;

    private SampleEnum enumerate;

}
