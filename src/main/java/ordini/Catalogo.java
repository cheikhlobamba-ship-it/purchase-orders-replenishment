package ordini;
import java.util.List;

public class Catalogo {
    public static List<Offerta> offerte() {
        return List.of(
            new Offerta("Fornitore 1", 120, 8, 5,
                    List.of(new ScontoSoglia(1000, 0.05, true))),
            new Offerta("Fornitore 2", 128, 15, 7,
                    List.of(new ScontoQuantita(new int[]{5, 10}, new double[]{0.03, 0.05}))),
            new Offerta("Fornitore 3", 129, 23, 4,
                    List.of(new ScontoSoglia(1000, 0.05, false), new ScontoMese(9, 0.02)))
        );
    }
}