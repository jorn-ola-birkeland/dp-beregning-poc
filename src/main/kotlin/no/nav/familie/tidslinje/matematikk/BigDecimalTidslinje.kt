package no.nav.familie.ba.sak.kjerne.tidslinje.matematikk

import no.nav.familie.ba.sak.kjerne.tidslinje.Tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.komposisjon.join
import no.nav.familie.ba.sak.kjerne.tidslinje.komposisjon.kombinerUtenNullOgIkkeTom
import no.nav.familie.ba.sak.kjerne.tidslinje.tidspunkt.Tidsenhet
import no.nav.familie.ba.sak.kjerne.tidslinje.transformasjon.mapIkkeNull
import java.math.BigDecimal
import java.math.RoundingMode

fun <K, T : Tidsenhet> Map<K, Tidslinje<BigDecimal, T>>.minus(
    bTidslinjer: Map<K, Tidslinje<BigDecimal, T>>,
) = this.join(bTidslinjer) { a, b ->
    when {
        a != null && b != null -> a - b
        else -> a
    }
}

fun <T : Tidsenhet> Tidslinje<BigDecimal, T>.rundAvTilHeltall(avrunding: RoundingMode = RoundingMode.HALF_UP) =
    this.mapIkkeNull { it.setScale(0, avrunding) }
