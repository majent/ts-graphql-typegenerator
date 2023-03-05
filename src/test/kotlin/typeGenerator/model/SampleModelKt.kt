package typeGenerator.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal

data class SampleModelKt<T>(
    @JsonIgnore
    val ignore: String,
    val nonnull: String,
    val string: String,
    val primitiveInt: Int,
    val integer: Int?,
    val primitiveLong: Long,
    val wrapperLong: Long?,
    val primitiveShort: Short,
    val wrapperShort: Short?,
    val primitiveDouble: Double,
    val wrapperDouble: Double?,
    val primitiveFloat: Float,
    val wrapperFloat: Float?,
    val primitiveByte: Byte,
    val wrapperByte: Byte?,
    val primitiveBoolean: Boolean,
    val wrapperBoolean: Boolean?,
    val bigDecimal: BigDecimal,
    val subModel: SampleSubModel<String>,
    val array: Array<String>,
    val listGenerics: List<T>,
    val listModel: List<SampleSubModel<T>>,
    val listString: List<String>,
    val listListString: List<List<String>>,
    val map: Map<String, Int>,
    val mapKeyEnumerate: Map<SampleEnum, Int>,
    val mapValueEnumerate: Map<SampleEnum, Int>,
    val enumerate: SampleEnum,
) : SampleInterface
