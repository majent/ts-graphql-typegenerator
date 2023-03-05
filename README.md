# Generate TypeScript type and Graphql Scheme from Java and Kotlin code.


## Usage

``` kotlin
val definitions = TypeGenerator.generate(classes = allClasses, packageName = " typeGenerator")
TypeGenerator.writeOut(definitions)
```

Source Code
```kotlin
data class Sample(
    val string: String,
    val integer: Int?,
    val boolean: Boolean,
    val subModel: SampleSub<String>,
    val list: List<SampleSub<String>>,
    val map: Map<String, Int>,
    val enumerate: SampleEnum,
) : SampleInterface


data class SampleSub<T>(
    val listGenerics: List<T>,
) : SampleInterface

```

Result (TypeScript) 
``` typescript
export type Sample = {
  string: string;
  integer?: number;
  boolean: boolean;
  list: SampleSub<string>[];
  subModel: SampleSub<string>;
  map: Record<string, number>;
  enumerate: SampleEnum;
};

export type SampleSub<T> = {
  listGenerics: T[];
};

export enum SampleEnum {
  Sample1 = 'SAMPLE1',
  Sample2 = 'SAMPLE2',
  Sample3 = 'SAMPLE3',
}

```

Result (GraphQL)

```graphql
type Sample {
  string: String!
  integer: Int
  boolean: Boolean!
  subModel: SampleSub!
  list: [SampleSub!]!
  map: [MapEntry!]!
  enumerate: SampleEnum!
}

type MapEntry {
  key : String!
  value : Int!
}

type SampleSub {
  listGenerics: [T!]!
}

enum SampleEnum {
  SAMPLE1
  SAMPLE2
  SAMPLE3
}
```