package pom.xml;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class Main {

    public static void main(String[] args) {
        Offerta o1 = new Offerta("Fornitore 1", 120, 8, 5,
                List.of(new ScontoSoglia(1000, 0.05, true)));

        Offerta o2 = new Offerta("Fornitore 2", 128, 15, 7,
                List.of(new ScontoQuantita(new int[]{5, 10}, new double[]{0.03, 0.05})));

        Offerta o3 = new Offerta("Fornitore 3", 129, 23, 4,
                List.of(new ScontoSoglia(1000, 0.05, false),
                        new ScontoMese(9, 0.02)));

        List<Offerta> offerte = List.of(o1, o2, o3);

        int quantita = 12;
        int mese = 9;
        List<Offerta> disponibili = new ArrayList<>();

        for (Offerta o : offerte) {
            if (o.pezziDisponibili >= quantita) {
                disponibili.add(o);
            }
        }

        disponibili.sort(Comparator.comparingDouble(o -> o.totaleNetto(quantita, mese)));

        for (int i = 0; i < disponibili.size(); i++) {
        Offerta o = disponibili.get(i);
        String etichetta = (i == 0) ? " è il più economico" : "";
        System.out.println(o.fornitore
                + " - netto " + String.format("%.2f", o.totaleNetto(quantita, mese)) + "€"
                + " - " + o.giorniSpedizione + " giorni"
                + etichetta);
}
    }
}