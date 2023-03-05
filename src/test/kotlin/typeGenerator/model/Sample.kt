package typeGenerator.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal

data class Sample(
    val string: String,
    val integer: Int?,
    val primitiveBoolean: Boolean,
    val subModel: SampleSub<String>,
    val list: List<SampleSub<String>>,
    val map: Map<String, Int>,
    val enumerate: SampleEnum,
) : SampleInterface
