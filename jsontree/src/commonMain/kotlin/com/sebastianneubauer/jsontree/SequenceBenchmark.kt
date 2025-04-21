package com.sebastianneubauer.jsontree

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@State(Scope.Benchmark)
public class SequenceBenchmark {

//  private val search = JsonTreeSearch(Dispatchers.Default)
//  private val list = List(1000) {
//    JsonTreeElement.Primitive(
//      id = "",
//      level = 0,
//      isLastItem = false,
//      key = "a",
//      value = JsonPrimitive("a"),
//      parentType = JsonTreeElement.ParentType.NONE
//    )
//  }

    @Param("Hello")
    public var test: String = ""

//  @Setup
//  public open fun prepare() {
//    test = "Hello"
//  }

    @Benchmark
    public fun listTest(blackhole: Blackhole) {
//    CoroutineScope(Dispatchers.Default).launch {
//      val result = search.search(
//        searchQuery = "a",
//        jsonTreeList = list
//      )

        val result: String = test + test
//      println("totalResults: ${result}")
        blackhole.consume(result)
//    }
    }
}
