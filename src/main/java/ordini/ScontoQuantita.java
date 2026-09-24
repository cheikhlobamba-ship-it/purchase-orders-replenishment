package pom.xml;
public class ScontoQuantita implements Sconto {
    int[] soglie;          
    double[] percentuali; 

    public ScontoQuantita(int[] soglie, double[] percentuali) {
        this.soglie = soglie;
        this.percentuali = percentuali;
    }

    @Override
    public double calcola(double lordo, int quantita, int mese) {
        double migliore = 0;

        for (int i = 0; i < soglie.length; i++) {
            if (quantita > soglie[i] && percentuali[i] > migliore) {
                migliore = percentuali[i];
            }
        }
        return migliore;
    }
}