package russianAnkiGen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Olli
 * @version 6.2.2020
 *
 */
public class Sanakortit {
    
    /**
     * 
     * @param args Ei Käytössä
     * @throws InterruptedException  Suoritus keskeytetty
     */
    public static void main(String args[]) throws InterruptedException {
        String lahdeteksti = lueTekstitiedostosta(args[0]);
        String tiedostoNimi = args[1];
        
        if(lahdeteksti.length() == 0) System.out.println("ERROR: File not found. Cannot proceed.");
        
        String[] sanat = poistaLyhyetSanat(etsiSanat(lahdeteksti), 3);
        Sanakirja substantiiviHaku = new Sanakirja(sanat, "russian-dictionary/nouns.csv", "noun");
        Sanakirja adjektiiviHaku = new Sanakirja(sanat, "russian-dictionary/adjectives.csv", "adjective");
        Sanakirja verbiHaku = new Sanakirja(sanat, "russian-dictionary/verbs.csv", "verb");
        
        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(substantiiviHaku);
        es.execute(adjektiiviHaku);
        es.execute(verbiHaku);
        es.shutdown();
        es.awaitTermination(5, TimeUnit.MINUTES);
        
        AnkiKortti[] substantiivit = substantiiviHaku.getAnkiKortit();
        AnkiKortti[] adjektiivit = adjektiiviHaku.getAnkiKortit();
        AnkiKortti[] verbit = verbiHaku.getAnkiKortit();
        
        int löydetytSanatLkm = substantiivit.length + adjektiivit.length + verbit.length;
        
        System.out.println("Word count: " + sanat.length);
        System.out.println("Words in nominative with translation: " + löydetytSanatLkm +
                " (Nouns:" + substantiivit.length + " Adjectives:" + adjektiivit.length + " Verbs:" + verbit.length + ")");
        
        luoUusiTiedosto(tiedostoNimi);
        lisaaTiedostoon(tiedostoNimi, substantiivit);
        lisaaTiedostoon(tiedostoNimi, adjektiivit);
        lisaaTiedostoon(tiedostoNimi, verbit);
        
    }

    private static void luoUusiTiedosto(String tiedostoNimi) {
        try {
            File tiedosto = new File(tiedostoNimi + ".txt");
            if (tiedosto.createNewFile()) {
              System.out.println("File created: " + tiedosto.getName());
            } else {
              System.out.println("File already exists.");
            }
        } 
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void lisaaTiedostoon(String tiedostoNimi, AnkiKortti[] kortit) {
        try {
            FileWriter fr = new FileWriter(tiedostoNimi + ".txt", true);
            for(AnkiKortti kortti: kortit) {
                fr.append(kortti.toString() + System.getProperty( "line.separator" ));
            }
            fr.close();
            System.out.println("Successfully wrote to the file.");
        } 
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Lukee tekstitiedoston ja palauttaa sen sisällön
     * @param tiedostopolku Tiedoston sijainti
     * @return Tiedoston sisältö
     */
    public static String lueTekstitiedostosta(String tiedostopolku) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(tiedostopolku), StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return contentBuilder.toString();
    }
    
    /**
     * Etsii sanan perusmuodon eli sanakirjamuodon annetusta csv-tiedostosta
     * @param sana Sana jonka perusmuotoa etsitään
     * @param tiedostopolku Polku csv-tiedostolle
     * @param rivi Rivinumero alkaen nollasta, jossa perusmuoto on
     * @return Sanan sanakirja muoto jos löytyy, muutoin null
     */
    public static String etsiSanakirjamuotoTiedostosta(String sana, String tiedostopolku, int rivi) {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(tiedostopolku))) {
            String row, sanakirjamuoto;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.replaceAll("\\s+", " ").split(" ");
                for(String str: data) {
                    if(sana.equalsIgnoreCase(str.replaceAll("'", ""))) {
                        sanakirjamuoto = data[rivi];
                        return sanakirjamuoto;
                    }
                }
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Etsii sanojen perusmuodon eli sanakirjamuodon annetusta csv-tiedostosta
     * @param sanat Sanat joiden perusmuodot etsitään
     * @param tiedostopolku Polku csv-tiedostolle
     * @param rivi Rivinumero alkaen nollasta, jossa perusmuoto on
     * @return Sanan sanakirja muoto jos löytyy, muutoin null
     */
    public static String[] etsiSanakirjamuodotTiedostosta(String sanat[], String tiedostopolku, int rivi) {
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
     * Poistaa taulukosta sanat joiden pituus on pienempi kuin annettu minimi
     * @param sanat Taulukko sanoista
     * @param minimiPituus Pienemmät kuin minimi poistetaan
     * @return Taulukko sanoista jotka ovat yhtäsuuria/pidempiä kuin minimi
     */
    public static String[] poistaLyhyetSanat(String[] sanat,int minimiPituus) {
        ArrayList<String> sanalista = new ArrayList<String>();
        for(String sana: sanat) {
            if(sana.length() >= minimiPituus) {
                sanalista.add(sana);
            }
        }
        String[] arr = new String[sanalista.size()];
        arr = sanalista.toArray(arr);
        return arr;
    }

    /**
     * Jakaa tekstin uniikkeihin sanoihin poistaen kaikki erikoismerkit ja palauttaa sen taulukkona
     * @param teksti Teksti josta sanoja etsitään
     * @return Sanat taulukkona
     */
    public static String[] etsiSanat(String teksti) {
        String[] sanat = poistaErikoismerkit(teksti).toLowerCase().split(" ");
        sanat = poistaMonikot(sanat);
        return sanat;
    }

    /**
     * Poistaa sanat talukosta jotka ovat jo siellä
     * @param sanat Sanat taulukossa
     * @return Uniikit sanat
     */
    public static String[] poistaMonikot(String[] sanat) {
        return Arrays.stream(sanat).distinct().toArray(String[]::new);
    }
    
    /**
     * Poistaa sanat talukosta jotka ovat jo siellä
     * @param sanat Sanat taulukossa
     * @return Uniikit sanat
     */
    public static AnkiKortti[] poistaMonikot(AnkiKortti[] sanat) {
        return Arrays.stream(sanat).distinct().toArray(AnkiKortti[]::new);
    }

    /**
     * Poistaa kaikki erikoismerkit tekstistä
     * @param teksti Teksti josta erikoismerkit poistetaan
     * @return Teksti ilman erikoismerkkejä
     */
    public static String poistaErikoismerkit(String teksti) {
        return teksti.replaceAll(",|!|\\?|\\.|—|:", "").trim().replaceAll("\\s+", " ");
    }
}
