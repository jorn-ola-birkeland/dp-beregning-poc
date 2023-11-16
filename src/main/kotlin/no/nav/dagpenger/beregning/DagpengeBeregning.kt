package no.nav.dagpenger.beregning

import no.nav.dagpenger.beregning.RapporteringDagType.Arbeid
import no.nav.familie.ba.sak.kjerne.tidslinje.Tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.eksperimentelt.filtrerMed
import no.nav.familie.ba.sak.kjerne.tidslinje.komposisjon.kombinerMed
import no.nav.familie.ba.sak.kjerne.tidslinje.komposisjon.kombinerUtenNullMed
import no.nav.familie.ba.sak.kjerne.tidslinje.tidslinje
import no.nav.familie.ba.sak.kjerne.tidslinje.tidspunkt.Dag
import no.nav.familie.ba.sak.kjerne.tidslinje.transformasjon.mapIkkeNull
import java.math.BigDecimal
import java.math.BigDecimal.TWO
import java.math.BigDecimal.ZERO
import java.math.BigDecimal.valueOf

class DagpengeBeregning(
    val dagsatsTidslinje: Tidslinje<Int, Dag>,
    val vanligArbeidstidPerDagTidslinje: Tidslinje<BigDecimal, Dag>,
    val rapporteringTidslinje: Tidslinje<Rapportering, Dag>
) {
    val timerJobbetTidslinje = rapporteringTidslinje.mapIkkeNull { it.timer }
    val harRettPåDagpengerTidslinje = rapporteringTidslinje.mapIkkeNull { it.harRettPåDagpenger() }
    val vanligArbeidstidMedRettTidslinje = vanligArbeidstidPerDagTidslinje.filtrerMed(harRettPåDagpengerTidslinje)

    val sumVanligArbeidtidMedRett = vanligArbeidstidMedRettTidslinje.sumVerdi()
    val sumTimerJobbet = timerJobbetTidslinje.sumVerdi()
    val harJobbetForMye = sumTimerJobbet.dividerMedEllersNull(sumVanligArbeidtidMedRett) > valueOf(0.5)

    val harUtbetalingTidslinje = if (harJobbetForMye) tidslinje() else rapporteringTidslinje
        .kombinerUtenNullMed(vanligArbeidstidPerDagTidslinje) { rapportering, arbeidstid ->
            when (rapportering.type) {
                Arbeid -> rapportering.timer < arbeidstid
                else -> rapportering.harRettPåDagpenger()
            }
        }

    val vanligArbeidstidMedUtbetalingTidslinje = vanligArbeidstidPerDagTidslinje
        .filtrerMed(harUtbetalingTidslinje)

    val gjenståendeDagpengetidTidslinje = vanligArbeidstidMedUtbetalingTidslinje
        .kombinerUtenNullMed(timerJobbetTidslinje) { vanlig, jobbet -> maxOf(vanlig.minus(jobbet), ZERO) }

    val overskytendeArbeidstimerTidslinje = vanligArbeidstidPerDagTidslinje
        .kombinerUtenNullMed(timerJobbetTidslinje) { vanlig, jobbet -> maxOf(jobbet.minus(vanlig), ZERO) }

    val gjenståendeDagpengetid = gjenståendeDagpengetidTidslinje.sumVerdi()
    val overskytendeArbeidstimer = overskytendeArbeidstimerTidslinje.sumVerdi()

    val taptArbeidstidTidslinje = gjenståendeDagpengetidTidslinje
        .mapIkkeNull { it - overskytendeArbeidstimer * it.dividerMedEllersNull(gjenståendeDagpengetid) }

    val utbetalingTidslinje = vanligArbeidstidMedUtbetalingTidslinje
        .kombinerUtenNullMed(taptArbeidstidTidslinje) { arbeid, tapt -> tapt.dividerMedEllersNull(arbeid) }
        .kombinerUtenNullMed(dagsatsTidslinje) { andel, sats -> andel * sats.toBigDecimal() }

    val avrundetUtbatalingTidslinje = utbetalingTidslinje
        .rundAvSumTilHeltallOgFordelRest()
}

