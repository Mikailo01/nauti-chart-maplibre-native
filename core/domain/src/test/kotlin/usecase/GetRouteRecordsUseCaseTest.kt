package usecase

import com.bytecause.domain.abstractions.TrackRouteRepository
import com.bytecause.domain.model.DateFilterOptions
import com.bytecause.domain.model.DistanceFilterOptions
import com.bytecause.domain.model.DurationFilterOptions
import com.bytecause.domain.model.RouteRecordModel
import com.bytecause.domain.model.SortOptions
import com.bytecause.domain.usecase.GetRouteRecordsUseCase
import com.google.common.truth.Truth.assertThat
import data.repository.FakeTrackRouteRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetRouteRecordsUseCaseTest {

    private lateinit var getRouteRecords: GetRouteRecordsUseCase
    private lateinit var fakeTrackRouteRepository: TrackRouteRepository

    private val testRouteRecords = listOf(
        RouteRecordModel(
            id = 1L,
            name = "Morning Run",
            description = "A short morning run",
            distance = 5.2,
            startTime = 1_625_562_000_000, // 1 hour
            dateCreated = 1_625_565_600_000,
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 2L,
            name = "Evening Walk",
            description = "A relaxing evening walk",
            distance = 60.0,
            startTime = 1_625_619_600_000, // 6 hours
            dateCreated = 1_625_641_200_000,
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 3L,
            name = "Trail Hike",
            description = "Challenging trail hike",
            distance = 10.0,
            startTime = 1_625_684_400_000, // 12 hours
            dateCreated = 1_625_727_600_000,
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 4L,
            name = "Beach Run",
            description = "A refreshing run along the beach",
            distance = 6.5,
            startTime = 1_625_727_600_000, // 1 day
            dateCreated = 1_625_814_000_000,
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 5L,
            name = "Mountain Climb",
            description = "Steep climb up the mountain",
            distance = 8.3,
            startTime = 1_625_295_600_000, // 1 week
            dateCreated = 1_625_900_400_000,
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 6L,
            name = "City Tour",
            description = "Leisurely city tour with scenic views",
            distance = 4.7,
            startTime = 1_625_986_800_000, // 1 hour
            dateCreated = 1_625_990_400_000,
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 7L,
            name = "Test Example",
            description = "Text description",
            distance = 80.0,
            startTime = System.currentTimeMillis() - 6_000_000, // 100 minutes
            dateCreated = System.currentTimeMillis(),
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 8L,
            name = "Test Example 2",
            description = "Text description 2",
            distance = 50.0,
            startTime = System.currentTimeMillis() - 1_296_000_000, // 7 days
            dateCreated = System.currentTimeMillis() - 691_200_000, // subtract 8 days
            points = emptyList(),
            speed = emptyMap()
        ),
        RouteRecordModel(
            id = 9L,
            name = "Test Example 2",
            description = "Text description 2",
            distance = 120.0,
            startTime = System.currentTimeMillis() - 3_369_600_000, // 7 days
            dateCreated = System.currentTimeMillis() - 2_764_800_000, // subtract 32 days
            points = emptyList(),
            speed = emptyMap()
        )
    )

    @Before
    fun setUp() {
        fakeTrackRouteRepository = FakeTrackRouteRepositoryImpl()
        getRouteRecords = GetRouteRecordsUseCase(fakeTrackRouteRepository)

        runBlocking {
            testRouteRecords.forEach { fakeTrackRouteRepository.saveRecord(it) }
        }
    }

    @Test
    fun `Order route records by name DESCENDING, correct order`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        for (i in 0..records.size - 2) {
            println(records[i].name)
            assertThat(records[i].name).isLessThan(records[i + 1].name.lowercase())
        }
    }

    @Test
    fun `Order route records by date created DESCENDING, correct order`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Recent,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        for (i in 0..records.size - 2) {
            assertThat(records[i].dateCreated).isGreaterThan(records[i + 1].dateCreated)
        }
    }

    @Test
    fun `Order route records by distance DESCENDING, correct order`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Distance,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        for (i in 0..records.size - 2) {
            assertThat(records[i].distance).isLessThan(records[i + 1].distance)
        }
    }

    @Test
    fun `Order route records by duration DESCENDING, correct order`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Duration,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        for (i in 0..records.size - 2) {
            assertThat(records[i].dateCreated - records[i].startTime)
                .isAtMost(records[i + 1].dateCreated - records[i + 1].startTime)
        }
    }

    @Test
    fun `Filter route records NO FILTER, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(testRouteRecords.size)
    }

    @Test
    fun `Filter route records from Today only, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.Today,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(1)
        assertThat(records.first().id).isEqualTo(7L)
    }

    @Test
    fun `Filter route records from Last Week only, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.Week,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(1)
    }

    @Test
    fun `Filter route records from This Month only, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.Month,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(2)
    }

    @Test
    fun `Filter route records from This Year only, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.Year,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(3)
    }

    @Test
    fun `Filter route records SHORT distance, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.Short,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(5)
    }

    @Test
    fun `Filter route records MID distance, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.Mid,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(6)
    }

    @Test
    fun `Filter route records LONG distance, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.Long,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(8)
    }

    @Test
    fun `Filter route records EXTRA LONG distance, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.ExtraLong,
            filter3 = DurationFilterOptions.All
        ).first()

        assertThat(records.size).isEqualTo(1)
        assertThat(records.first().id).isEqualTo(9L)
    }

    @Test
    fun `Filter route records ONE HOUR duration, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.OneHour
        ).first()

        assertThat(records.size).isEqualTo(2)
    }

    @Test
    fun `Filter route records SIX HOURS duration, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.SixHours
        ).first()

        assertThat(records.size).isEqualTo(4)
    }

    @Test
    fun `Filter route records TWELVE HOURS duration, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.TwelveHours
        ).first()

        assertThat(records.size).isEqualTo(5)
    }

    @Test
    fun `Filter route records ONE DAY duration, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.Day
        ).first()

        assertThat(records.size).isEqualTo(6)
    }

    @Test
    fun `Filter route records MORE THAN DAY duration, correct elements`() = runBlocking {
        val records = getRouteRecords(
            sorter = SortOptions.Name,
            filter1 = DateFilterOptions.All,
            filter2 = DistanceFilterOptions.All,
            filter3 = DurationFilterOptions.MoreThanDay
        ).first()

        assertThat(records.size).isEqualTo(3)
    }
}