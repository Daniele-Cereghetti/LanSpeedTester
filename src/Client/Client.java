package Client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import Helpers.ValidateInputs;

/**
 * Classe che permette ad un calcolatore di assumere il ruolo di un client.
 *
 * @author Daniele Cereghetti
 * @version 26.11.2020
 */
public class Client {

    /**
     * Attirbuto che indica il numero di connessioni da testare.
     */
    private int connactions;

    /**
     * Attibuto che indica l'ip del server con cui andare a fare il test.
     */
    private Inet4Address ipServer;

    /**
     * Attributo che indica la porta su cui il server si aprirà.
     */
    private int serverPort;

    /**
     * Attributo che contiene i tempi per la statistica.
     */
    private BlockingQueue<Long> times;

    /**
     * Attributo che indica la dimensione dei dati.
     */
    private int dimensionData;

    /**
     * Attributo che indica la porta di ascolto del client.
     */
    private int port;

    /**
     * Attributo che indica le porte del server con coi farà il test.
     */
    private int[] serverPorts;

    /**
     * Attributo che indica le porte del client con coi farà il test.
     */
    private int[] clientPorts;

    /**
     * Ottieni il valore di myPort.
     *
     * @return il valore di myPort
     */
    public int getPort() {
        return port;
    }

    /**
     * Ottieni il valore di dimData.
     *
     * @return il valore di dimData
     */
    public int getDimensionData() {
        return dimensionData;
    }

    /**
     * Ottieni il valore di port.
     *
     * @return il valore di port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Ottieni il valore di ipServer.
     *
     * @return il valore di ipServer
     */
    public Inet4Address getIpServer() {
        return ipServer;
    }

    /**
     * Ottieni il valore di nConnaction.
     *
     * @return il valore di nConnaction
     */
    public int getConnactions() {
        return connactions;
    }

    /**
     * Ottieni il valore di tempi.
     *
     * @return il valore di tempi
     */
    public BlockingQueue<Long> getTimes() {
        return times;
    }

    /**
     * Imposta il valore di ipServer.
     *
     * @param ipServer nuovo valore di ipServer
     */
    public void setIpServer(Inet4Address ipServer) {
        this.ipServer = ipServer;
    }

    /**
     * Imposta il valore di nConnaction.
     *
     * @param connactions nuovo valore di nConnaction
     */
    public void setConnactions(int connactions) {
        this.connactions = connactions;
    }

    /**
     * Imposta il valore di port.
     *
     * @param serverPort nuovo valore di port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Imposta il valore di dimData.
     *
     * @param dimensionData nuovo valore di dimData
     */
    public void setDimensionData(int dimensionData) {
        this.dimensionData = dimensionData;
    }

    /**
     * Costruttore personalizzato.
     *
     * @param connactions numero di connessioni da fare
     * @param ipServer ip del server
     * @param serverPort porta del sever sulla quale è in ascolto
     * @param dimensionData dimensione dei dati da inviare
     */
    public Client(int connactions, Inet4Address ipServer, int serverPort, int dimensionData) {
        this.connactions = connactions;
        this.ipServer = ipServer;
        this.serverPort = serverPort;
        this.dimensionData = dimensionData;
        times = new LinkedBlockingQueue<>();
    }

    /**
     * Metodo che si occupa di verificare l'esistenza del server, inoltre invia
     * e riceve i dati utili per il test.
     *
     * @return client connesso al server
     */
    public boolean testServer() {
        try {
            //apro connessione verso il server
            Socket client = new Socket(getIpServer(), getServerPort());
            //salvo la porta sulla quale sto ricevendo i dati
            this.port = client.getLocalPort();
            //preparo la richiesta dati base
            OutputStream output = client.getOutputStream();
            PrintWriter data = new PrintWriter(output, true);
            //faccio la richiesta porte client
            String ports = ValidateInputs.getAviablePort(
                    port, getConnactions() + 1).toString();
            data.println(getConnactions() + ";" + getDimensionData());
            data.println(ports);
            //componenti per leggere porte disp server
            InputStream input = client.getInputStream();
            BufferedReader dataServer = new BufferedReader(
                    new InputStreamReader(input)
            );
            //trasformazione dati porte server
            String testo = dataServer.readLine();
            String[] svr = testo.split(",");
            String[] cli = ports.split(",");
            serverPorts = new int[getConnactions()];
            clientPorts = new int[getConnactions()];
            for (int i = 0; i < getConnactions(); i++) {
                if (!svr[i].isEmpty()) {
                    serverPorts[i] = Integer.parseInt(svr[i]);
                }
                if (!cli[i].isEmpty()) {
                    clientPorts[i] = Integer.parseInt(cli[i]);
                }
            }
            //chiusura connessione
            client.close();
            return true;
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        return false;
    }

    /**
     * Metodo che si occupa di creare le thread che faranno il test.
     */
    public void createThread() {
        for (int i = 0; i < getConnactions(); i++) {
            new ClientThread(
                    clientPorts[i],
                    getIpServer(),
                    serverPorts[i],
                    getDimensionData(),
                    times
            ).start();
        }
    }

    /**
     * Metodo che si occupa di fare la statistica sui dati del test.
     */
    public void doStatistics() {
        //lettura dei tempi
        List<Long> numbers = new ArrayList();
        try {
            int timesLength = times.size();
            for (int i = 0; i < timesLength; i++) {
                numbers.add(times.take());
            }
        } catch (InterruptedException e) {
        }
        //pacchetti ricevuti + persi
        int pacchettiTrasmessi = numbers.size();
        int pacchettiRivevuti = 0;
        int pacchettiPersi = 0;
        int sommaTempi = 0;
        int minTempo = 2000;//tempo massimo del delay
        int maxTempo = 0;
        for (long value : numbers) {
            if (value != -1) {
                pacchettiRivevuti++;
                sommaTempi += value;
                if (maxTempo < value) {
                    maxTempo = (int) value;
                }
                if (minTempo > value) {
                    minTempo = (int) value;
                }
            } else {
                pacchettiPersi++;
            }
        }
        //Media dei tempi (milli secondi)
        double media = sommaTempi / pacchettiTrasmessi;
        //statistica
        String statistica = "\nStatistica:\n";
        //info pacchetti
        statistica += "Pacchetti trasmessi = " + pacchettiTrasmessi + "\n";
        statistica += "Pacchetti ricevuti = " + pacchettiRivevuti + "(";
        statistica += (int) (((double) pacchettiRivevuti / (double) pacchettiTrasmessi) * 100) + "%)\n";
        statistica += "Pacchetti persi = " + pacchettiPersi + "(";
        statistica += (int) (((double) pacchettiPersi / (double) pacchettiTrasmessi) * 100) + "%)\n";
        //info tempi
        statistica += "\nTempi:\n";
        statistica += "Media = " + media + " ms \n";
        statistica += "Minimo = " + minTempo + " ms \n";
        statistica += "Massimo = " + maxTempo + " ms \n";
        //velocità banda --> (bit trasmessi in totale / tempo impiegato totale) bit/s
        long velocita = (long) ((long) (getDimensionData() * 8 * 10 * getConnactions()) / ((double) (sommaTempi) / 1000));
        String f = "" + velocita;
        int i = f.length();
        statistica += "\nVelocita' rete = ";
        if (i < 4) {
            statistica += velocita + " b/s";
        } else if (i < 7) {
            velocita /= 1000;
            statistica += velocita + " Kb/s";
        } else if (i < 10) {
            velocita /= 1000000;
            statistica += velocita + " Mb/s";
        } else if (i < 13) {
            velocita /= 1000000000;
            statistica += velocita + " Gb/s";
        }
        System.out.println(statistica);
        if (connactions == 1) {
            System.out.println("\nNon si ha sfruttato il multithreading :(");
        } else {
            System.out.println("\nÈ stato sfruttato il multithreading :)");
        }
    }
}
