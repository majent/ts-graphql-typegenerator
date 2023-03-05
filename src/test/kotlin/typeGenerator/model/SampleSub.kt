package typeGenerator.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal

data class SampleSub<T>(
    val listGenerics: List<T>,
) : SampleInterface
