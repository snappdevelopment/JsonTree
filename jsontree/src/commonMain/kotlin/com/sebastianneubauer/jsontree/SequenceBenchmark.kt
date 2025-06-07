package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.search.JsonTreeSearch
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SequenceBenchmark {

    private val search = JsonTreeSearch(Dispatchers.Default)
    private val list = List(1000) {
        JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = false,
            key = "a",
            value = JsonPrimitive("a"),
            parentType = JsonTreeElement.ParentType.NONE
        )
    }

    @Benchmark
    public fun listTest(blackhole: Blackhole) {
        CoroutineScope(Dispatchers.Default).launch {
            val result = search.search(
                searchQuery = "a",
                jsonTreeList = list
            )

            blackhole.consume(result)
        }
    }
}
