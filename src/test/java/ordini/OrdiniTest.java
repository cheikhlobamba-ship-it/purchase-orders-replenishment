package src.test.java.ordini;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica il calcolo degli ordini di acquisto contro i casi indicati
 * nella specifica, più alcuni casi limite non coperti dagli esempi.
 */
class OrdiniTest {

    private static final double TOLLERANZA = 0.01;

    private static final int SETTEMBRE = 9;
    private static final int NOVEMBRE = 11;

    private Offerta fornitore(String nome) {
        return Catalogo.offerte().stream()
                .filter(o -> o.fornitore.equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fornitore non trovato: " + nome));
    }

    private List<Offerta> disponibiliOrdinatePerPrezzo(int quantita, int mese) {
        return Catalogo.offerte().stream()
                .filter(o -> o.pezziDisponibili >= quantita)
                .sorted(Comparator.comparingDouble(o -> o.totaleNetto(quantita, mese)))
                .toList();
    }

    // ---------------------------------------------------------------
    // Esempio 1 della specifica: ordine di 12 pezzi a settembre
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Esempio 1: il fornitore senza stock sufficiente viene escluso")
    void fornitoreSenzaStockEscluso() {
        List<Offerta> disponibili = disponibiliOrdinatePerPrezzo(12, SETTEMBRE);

        assertEquals(2, disponibili.size());
        assertTrue(disponibili.stream().noneMatch(o -> o.fornitore.equals("Fornitore 1")));
    }

    @Test
    @DisplayName("Esempio 1: il Fornitore 2 applica solo la fascia di sconto migliore")
    void fornitore2Settembre() {
        assertEquals(1459.20, fornitore("Fornitore 2").totaleNetto(12, SETTEMBRE), TOLLERANZA);
    }

    @Test
    @DisplayName("Esempio 1: il Fornitore 3 applica lo sconto stagionale in cascata")
    void fornitore3Settembre() {
        assertEquals(1441.19, fornitore("Fornitore 3").totaleNetto(12, SETTEMBRE), TOLLERANZA);
    }

    @Test
    @DisplayName("Esempio 1: a settembre il più economico è il Fornitore 3")
    void migliorFornitoreSettembre() {
        List<Offerta> disponibili = disponibiliOrdinatePerPrezzo(12, SETTEMBRE);

        assertEquals("Fornitore 3", disponibili.get(0).fornitore);
    }

    // ---------------------------------------------------------------
    // Esempio 2 della specifica: stesso ordine a novembre
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Esempio 2: fuori stagione il Fornitore 3 perde il 2% aggiuntivo")
    void fornitore3Novembre() {
        assertEquals(1470.60, fornitore("Fornitore 3").totaleNetto(12, NOVEMBRE), TOLLERANZA);
    }

    @Test
    @DisplayName("Esempio 2: a novembre il più economico diventa il Fornitore 2")
    void migliorFornitoreNovembre() {
        List<Offerta> disponibili = disponibiliOrdinatePerPrezzo(12, NOVEMBRE);

        assertEquals("Fornitore 2", disponibili.get(0).fornitore);
    }

    @Test
    @DisplayName("Esempio 2: il più economico non è il più veloce")
    void ilPiuEconomicoNonEIlPiuVeloce() {
        List<Offerta> disponibili = disponibiliOrdinatePerPrezzo(12, NOVEMBRE);

        Offerta piuEconomico = disponibili.get(0);
        Offerta piuVeloce = disponibili.stream()
                .min(Comparator.comparingInt(o -> o.giorniSpedizione))
                .orElseThrow();

        assertNotEquals(piuEconomico.fornitore, piuVeloce.fornitore);
    }

    // ---------------------------------------------------------------
    // Casi limite non coperti dagli esempi
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Lo sconto per mese si applica solo a settembre")
    void scontoStagionaleSoloASettembre() {
        Offerta terzo = fornitore("Fornitore 3");

        double settembre = terzo.totaleNetto(12, SETTEMBRE);
        double gennaio = terzo.totaleNetto(12, 1);
        double dicembre = terzo.totaleNetto(12, 12);

        assertTrue(settembre < gennaio);
        assertEquals(gennaio, dicembre, TOLLERANZA);
    }

    @Test
    @DisplayName("Uno stock esattamente pari alla quantità richiesta è sufficiente")
    void stockEsattamenteSufficiente() {
        Offerta primo = fornitore("Fornitore 1");

        List<Offerta> disponibili = disponibiliOrdinatePerPrezzo(primo.pezziDisponibili, SETTEMBRE);

        assertTrue(disponibili.stream().anyMatch(o -> o.fornitore.equals("Fornitore 1")));
    }

    @Test
    @DisplayName("Sotto la soglia in euro non viene applicato alcuno sconto")
    void nessunoScontoSottoLaSoglia() {
        // 8 x 120 = 960 euro, sotto la soglia di 1000
        assertEquals(960.00, fornitore("Fornitore 1").totaleNetto(8, SETTEMBRE), TOLLERANZA);
    }

    @Test
    @DisplayName("Sulla soglia esatta 'minimum' include, 'over' esclude")
    void distinzioneTraMinimumEOver() {
        Sconto minimo = new ScontoSoglia(1000, 0.05, true);
        Sconto oltre = new ScontoSoglia(1000, 0.05, false);

        assertEquals(0.05, minimo.calcola(1000, 10, SETTEMBRE), TOLLERANZA);
        assertEquals(0.00, oltre.calcola(1000, 10, SETTEMBRE), TOLLERANZA);
    }
}