package Client;

import java.net.*;
import java.io.*;
import java.nio.channels.IllegalBlockingModeException;
import java.util.concurrent.BlockingQueue;

/**
 * Classe che si occupa di gestire il processo del client.
 *
 * @author Daniele Cereghetti
 * @version 19.11.2020
 */
public class ClientThread extends Thread {

    /**
     * Attributo che indica l'ip del server.
     */
    private Inet4Address ipServer;

    /**
     * Attiburo che indica la porta sulla quale il server sta ascoltando.
     */
    private int serverPort;

    /**
     * Attributo che indica la dimensione dei dati.
     */
    private int dimensionData;

    /**
     * Attributo che contiene i tempi per la statistica.
     */
    private BlockingQueue times;

    /**
     * Attributo che indica la porta di ascolto del client
     */
    private int myPort;

    /**
     * Ottini il valore di myPort
     *
     * @return il valore di myPort
     */
    public int getMyPort() {
        return myPort;
    }

    /**
     * Ottini il valore di serverPort
     *
     * @return il valore di serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Ottini il valore di ipServer
     *
     * @return il valore di ipServer
     */
    public Inet4Address getIpServer() {
        return ipServer;
    }

    /**
     * Ottini il valore di dimensionData
     *
     * @return il valore di dimensionData
     */
    public int getDimensionData() {
        return dimensionData;
    }

    /**
     * Costruttore personalizzato.
     *
     * @param myPort porta del client
     * @param ipServer ip del server
     * @param serverPort porta del server
     * @param dimensionData dimensione dei dati
     * @param times lista per i tempi di trasmissione
     */
    public ClientThread(int myPort, Inet4Address ipServer, int serverPort, int dimensionData, BlockingQueue times) {
        this.myPort = myPort;
        this.ipServer = ipServer;
        this.serverPort = serverPort;
        this.dimensionData = dimensionData;
        this.times = times;
    }

    /**
     * Metodo che esegue le azioni che farà la thread.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(500);
            DatagramSocket clientUDP = new DatagramSocket(getMyPort());
            byte[] data = new byte[getDimensionData()];
            clientUDP.setSoTimeout(2000);
            int i = 0;
            while (i < 10) {
                try {
                    //preparazione pacchetti
                    DatagramPacket dataServer = new DatagramPacket(
                            data, data.length, getIpServer(), getServerPort()
                    );
                    //mando pacchetti + cronometro
                    long start = System.currentTimeMillis();
                    clientUDP.send(dataServer);
                    clientUDP.receive(dataServer);
                    long end = System.currentTimeMillis();
                    times.put((end - start)/2);
                    /*divido x 2 perché verifico il tempo tra client e server
                     * quindi il dato che ricevo sarebbe andata e ritorno
                     * e quindi faccio la media dei due tempi
                     */
                } catch (SocketTimeoutException ste) {
                    try {
                        times.put((long)(-1));
                    } catch (InterruptedException ex) {
                    
                    }
                }
                i++;
            }
            clientUDP.close();
        } catch (IOException | IllegalBlockingModeException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
