package exercism.kotlin.autotests.runner.report

import exercism.kotlin.autotests.executor.ExecutionResult
import exercism.kotlin.autotests.executor.ExecutionResult.Status.Error
import exercism.kotlin.autotests.executor.ExecutionResult.Status.Fail
import exercism.kotlin.autotests.executor.ExecutionResult.Status.Success
import utils.junit.TestCase
import utils.junit.TestSuit
import java.io.File

fun ExecutionResult.exportReportToFile(file: File) {
    val report = asReport()
    file.writeText(report.asJson())
}

private fun ExecutionResult.asReport(): Report =
    Report(
        status = status.asReportStatus(),
        message = null,
        tests = suits.asReportTestEntries()
    )

private fun ExecutionResult.Status.asReportStatus(): Report.Status =
    when (this) {
        Success -> Report.Status.Pass
        Fail -> Report.Status.Fail
        Error -> Report.Status.Error
    }

private fun List<TestSuit>.asReportTestEntries(): List<Report.TestEntry> =
    flatMap { it.testCases }
        .map(TestCase::asReportTestEntry)

private fun TestCase.asReportTestEntry(): Report.TestEntry =
    Report.TestEntry(
        name = name,
        status = if (failure == null) Report.Status.Pass else Report.Status.Fail,
        message = getMessage(),
        output = failure?.stackTrace ?: "" // TODO output is available only for test suit in JUnit4
    )

private fun TestCase.getMessage(): String {
    if (failure == null) return ""

    val rawMessage = failure.message
    return when (failure.type) {
        "org.junit.ComparisonFailure" -> rawMessage.substringAfter("org.junit.ComparisonFailure:").trim()
        else -> rawMessage
    }
}
