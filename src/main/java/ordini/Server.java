package ordini;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.InputStream;
public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", Server::paginaHome);
        server.createContext("/style.css", Server::foglioStile);
        server.createContext("/risultati", Server::risultati);
        server.start();
        System.out.println("Server avviato: apri http://localhost:8080");
    }
    static String leggiRisorsa(String nome) throws IOException {
        try (InputStream in = Server.class.getResourceAsStream("/" + nome)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void paginaHome(HttpExchange scambio) throws IOException {
        invia(scambio, leggiRisorsa("index.html"), "text/html");
    }

    static void foglioStile(HttpExchange scambio) throws IOException {
        invia(scambio, leggiRisorsa("style.css"), "text/css");
    }

    static void risultati(HttpExchange scambio) throws IOException {
        // "quantita=12&mese=9" → prendo i due numeri
        String[] parti = scambio.getRequestURI().getQuery().split("&");
        int quantita = Integer.parseInt(parti[0].split("=")[1]);
        int mese = Integer.parseInt(parti[1].split("=")[1]);

        // filtro: disponibili ed esclusi
        List<Offerta> disponibili = new ArrayList<>();
        List<Offerta> esclusi = new ArrayList<>();
        for (Offerta o : Catalogo.offerte()) {
            if (o.pezziDisponibili >= quantita) {
                disponibili.add(o);
            } else {
                esclusi.add(o);
            }
        }

        // ordinamento dal più economico
        disponibili.sort(Comparator.comparingDouble(o -> o.totaleNetto(quantita, mese)));

        String html = "<!DOCTYPE html><html lang='it'><head><meta charset='utf-8'>"
                + "<link rel='stylesheet' href='/style.css'><title>Risultati</title></head><body>";
        html += "<h1>Risultati per " + quantita + " pezzi</h1>";

        if (disponibili.isEmpty()) {
            html += "<p>Nessun fornitore ha abbastanza pezzi.</p>";
        }

        // una card per ogni fornitore disponibile
        for (int i = 0; i < disponibili.size(); i++) {
            Offerta o = disponibili.get(i);
            String classe = (i == 0) ? "card migliore" : "card";
            html += "<div class='" + classe + "'>";
            if (i == 0) {
                html += "<span class='badge'>Più economico</span>";
            }
            html += "<h2>" + o.fornitore + "</h2>";
            html += "<p class='prezzo'>" + String.format("%.2f", o.totaleNetto(quantita, mese)) + " €</p>";
            html += "<p>Lordo: " + String.format("%.2f", o.totaleLordo(quantita)) + " €</p>";
            html += "<p>Spedizione: " + o.giorniSpedizione + " giorni</p>";
            html += "</div>";
        }

        // i fornitori esclusi, con il motivo
        for (Offerta o : esclusi) {
            html += "<div class='card escluso'>";
            html += "<h2>" + o.fornitore + "</h2>";
            html += "<p>Escluso: solo " + o.pezziDisponibili + " pezzi disponibili su " + quantita + "</p>";
            html += "</div>";
        }

        html += "<a href='/'>Nuova ricerca</a></body></html>";
        invia(scambio, html, "text/html");
    }

    static void invia(HttpExchange scambio, String testo, String tipo) throws IOException {
        byte[] dati = testo.getBytes(StandardCharsets.UTF_8);
        scambio.getResponseHeaders().set("Content-Type", tipo + "; charset=utf-8");
        scambio.sendResponseHeaders(200, dati.length);
        scambio.getResponseBody().write(dati);
        scambio.close();
    }
}