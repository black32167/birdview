package org.birdview.utils

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class ReflectiveParameterResolverTest {
    companion object {
        const val STRING_PARAM = "stringParam"
        const val ENUM_PARAM = "enumParam"
        const val OBJECT_PARAM1 = "objectParam1"
        const val OBJECT_PARAM2 = "objectParam2"
    }

    enum class TestEnum {
        ENUM_VALUE1, ENUM_VALUE2
    }

    class TestObject (val objectParam1: String, val objectParam2: TestEnum)

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = ENUM_PARAM)
    @JsonSubTypes(
        JsonSubTypes.Type(value = PolimorficTestObject1::class, name = "ENUM_VALUE1"),
        JsonSubTypes.Type(value = PolimorficTestObject2::class, name = "ENUM_VALUE2"))
    abstract class PolimorficTestObjectBaseClass (val objectParam1: String, val objectParam2: TestEnum)
    class PolimorficTestObject1 (objectParam1: String, objectParam2: TestEnum): PolimorficTestObjectBaseClass(objectParam1, objectParam2)
    class PolimorficTestObject2 (objectParam1: String, objectParam2: TestEnum): PolimorficTestObjectBaseClass(objectParam1, objectParam2)

    private val stringValuesMap = mapOf(
        STRING_PARAM to "value1",
        ENUM_PARAM to TestEnum.ENUM_VALUE2.name,
        OBJECT_PARAM1 to "value1",
        OBJECT_PARAM2 to TestEnum.ENUM_VALUE1.name
    )
    private val resolver = ReflectiveParameterResolver  {name -> stringValuesMap[name]!!}

    @Test
    fun testStringParameterResolution() {
        val resolvedValue = resolver.resolve(STRING_PARAM, String::class)
        assertEquals(stringValuesMap[STRING_PARAM], resolvedValue)
    }

    @Test
    fun testEnumParameterResolution() {
        val resolvedValue = resolver.resolve(ENUM_PARAM, TestEnum::class)
        assertEquals(TestEnum.ENUM_VALUE2, resolvedValue)
    }

    @Test
    fun testObjectParameterResolution() {
        val resolvedValue = resolver.resolve("doesnotmatter", TestObject::class)
        assertThat(resolvedValue)
            .usingRecursiveComparison()
            .isEqualTo(TestObject("value1", TestEnum.ENUM_VALUE1))
    }

    @Test
    fun testPolimorphicObjectResolution() {
        val resolvedValue = resolver.resolve("doesnotmatter", PolimorficTestObjectBaseClass::class)
        assertThat(resolvedValue)
            .usingRecursiveComparison()
            .isEqualTo(PolimorficTestObject2("value1", TestEnum.ENUM_VALUE1))
    }
}
