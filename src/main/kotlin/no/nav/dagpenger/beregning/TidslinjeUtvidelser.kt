package no.nav.dagpenger.beregning

import no.nav.familie.ba.sak.kjerne.tidslinje.Tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.komposisjon.innholdForTidspunkt
import no.nav.familie.ba.sak.kjerne.tidslinje.komposisjon.kombinerMed
import no.nav.familie.ba.sak.kjerne.tidslinje.tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.tidspunkt.Tidsenhet
import no.nav.familie.ba.sak.kjerne.tidslinje.tidsrom
import no.nav.familie.ba.sak.kjerne.tidslinje.tilPeriodeMedInnhold
import no.nav.familie.ba.sak.kjerne.tidslinje.transformasjon.mapIkkeNull
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

fun <I, T : Tidsenhet, R> Tidslinje<I, T>.foldVerdi(initielt: R, acc: (R, I) -> R) =
    this.tidsrom()
        .map { this.innholdForTidspunkt(it) }
        .filter { it.harVerdi }
        .map { it.verdi }
        .fold(initielt, acc)

fun <T : Tidsenhet> Tidslinje<Int, T>.sumVerdi(initielt: Int = 0) =
    this.foldVerdi(initielt) { acc, v -> acc + v }

fun <T : Tidsenhet> Tidslinje<BigDecimal, T>.sumVerdi(initielt: BigDecimal = BigDecimal.ZERO) =
    this.foldVerdi(initielt) { acc, v -> acc + v }

fun BigDecimal.dividerMedEllersNull(divisor: BigDecimal) =
    if (divisor.abs() > BigDecimal.ZERO) this.divide(divisor, MathContext.DECIMAL128) else BigDecimal.ZERO

fun <T : Tidsenhet> Tidslinje<BigDecimal, T>.rundAvSumTilHeltallOgFordelRest(avrunding: RoundingMode = RoundingMode.HALF_UP): Tidslinje<Int, T> {
    val totalAvrundet = this.sumVerdi().setScale(0, avrunding).intValueExact()

    val avrundetNedTidslinje = this.mapIkkeNull { it.setScale(0, RoundingMode.DOWN).intValueExact() }
    val sumHverAvrundetNed = avrundetNedTidslinje.sumVerdi()

    val rest = totalAvrundet - sumHverAvrundetNed

    val restTidslinje = tidslinje {
        this.tidsrom().map { tidspunkt -> tidspunkt to this.innholdForTidspunkt(tidspunkt) }
            .filter { it.second.harVerdi }
            .sortedByDescending { it.second.verdi.remainder(BigDecimal.ONE) }
            .take(rest)
            .map { it.first.tilPeriodeMedInnhold(1) }
    }

    return avrundetNedTidslinje.kombinerMed(restTidslinje) { avrundetNed, rest ->
        when {
            avrundetNed != null && rest != null -> avrundetNed + rest
            else -> avrundetNed
        }
    }
}

