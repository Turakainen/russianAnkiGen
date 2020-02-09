package russianAnkiGen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Luokka sanojen muuttamiseen sanakirja muotoon
 * @author Olli
 * @version 7.2.2020
 */
public class Sanakirja implements Runnable {

    private Thread thread;
    private String sanakirjanTiedostopolku;
    private String tunniste;
    private String[] sanat;
    private String[] sanakirjaMuodot;
    private AnkiKortti[] ankiKortit;
    
    /**
     * Konstruktori
     * @param sanat Sanat jotka muutetaan
     * @param sanakirjanTiedostopolku Tiedostopolku sanakirjan tietokannalle
     * @param tunniste Kortteihin lisättävä tunniste
     */
    public Sanakirja(String[] sanat, String sanakirjanTiedostopolku, String tunniste) {
        this.sanat = sanat;
        this.sanakirjanTiedostopolku = sanakirjanTiedostopolku;
        this.tunniste = tunniste;
    }
    
    @Override
    public void run() {
        //sanakirjaMuodot = Sanakortit.poistaMonikot(etsiSanakirjamuodotTiedostosta(sanakirjanTiedostopolku, 0));
        ankiKortit = Sanakortit.poistaMonikot(muutaAnkiKorteiksi(sanakirjanTiedostopolku, 0, 2, tunniste));
    }

    /**
     * Käynnistää sanojen muuttamisen perusmuotoon
     */
    public void start() {
        if(thread == null) {
            thread = new Thread(this, sanakirjanTiedostopolku);
            thread.start();
        }
    }
    
    /**
     * Etsii sanojen perusmuodon ja käännöksen ja muodostaa niistä ankikortin.
     * @param tiedostopolku Polku csv-tiedostolle
     * @param etupuoliRivi Rivinumero alkaen nollasta, jossa kortin etupuoleen haluttu teksi sijaitsee
     * @param takapuoliRivi Rivinumero alkaen nollasta, jossa kortin takapuolen haluttu teksi sijaitsee
     * @param tunniste Kortin hakutunniste
     * @return Ankikortit taulukkona
     */
    public AnkiKortti[] muutaAnkiKorteiksi(String tiedostopolku, int etupuoliRivi, int takapuoliRivi, String tunniste) {
        ArrayList<AnkiKortti> perusmuodot = new ArrayList<AnkiKortti>();
        try (BufferedReader csvReader = new BufferedReader(new FileReader(tiedostopolku))) {
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split("\\t");
                for(String str: data) {
                    if(Arrays.stream(sanat).anyMatch(str.replaceAll("'", "")::equals)) {
                        perusmuodot.add(new AnkiKortti(data[etupuoliRivi], data[takapuoliRivi], tunniste));
                    }
                }
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnkiKortti[] arr = new AnkiKortti[perusmuodot.size()];
        arr = perusmuodot.toArray(arr);
        return arr;
    }
    
    /**
     * Etsii sanojen perusmuodon eli sanakirjamuodon annetusta csv-tiedostosta
     * @param tiedostopolku Polku csv-tiedostolle
     * @param rivi Rivinumero alkaen nollasta, jossa perusmuoto on
     * @return Sanan sanakirja muoto jos löytyy, muutoin null
     */
    public String[] etsiSanakirjamuodotTiedostosta(String tiedostopolku, int rivi) {
        ArrayList<String> perusmuodot = new ArrayList<String>();
        try (BufferedReader csvReader = new BufferedReader(new FileReader(tiedostopolku))) {
            String row, sanakirjamuoto;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.replaceAll("\\s+", " ").split(" ");
                for(String str: data) {
                    if(Arrays.stream(sanat).anyMatch(str.replaceAll("'", "")::equals)) {
                        sanakirjamuoto = data[rivi];
                        perusmuodot.add(sanakirjamuoto);
                    }
                }
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] arr = new String[perusmuodot.size()];
        arr = perusmuodot.toArray(arr);
        return arr;
    }

    /**
     * @return Palauttaa luokan perusmuotoon muuttamat sanat
     */
    public String[] getSanakirjaMuodot() {
        return sanakirjaMuodot;
    }

    /**
     * @return Palauttaa luokan muodostamat ankikortit
     */
    public AnkiKortti[] getAnkiKortit() {
        return ankiKortit;
    }
}
