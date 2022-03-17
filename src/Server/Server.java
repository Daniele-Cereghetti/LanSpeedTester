package Server;

import java.net.*;
import java.io.*;
import Helpers.ValidateInputs;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Classe che permette ad un calcolatore di assumere il ruolo di server.
 *
 * @author Daniele Cereghetti
 * @version 19.11.2020
 */
public class Server {

    /**
     * Attiburo che indica la porta sulla quale il server sta ascoltando.
     */
    private int port;

    /**
     * Numero di connessioni che il server dovra aprire.
     */
    private int threads;

    /**
     * Attriburo che indica l'ip del client.
     */
    private Inet4Address ipClient;

    /**
     * Attributo che indica la dimensione dei dati.
     */
    private int dimensionData;

    /**
     * Attributo che indica le porte del client con coi farà il test.
     */
    private int[] clientPorts;

    /**
     * Attributo che indica le porte del server con coi farà il test.
     */
    private int[] serverPorts;

    /**
     * Ottieni il valore di port
     *
     * @return the value of port
     */
    public int getPort() {
        return port;
    }

    /**
     * Ottieni il valore di nThread.
     *
     * @return the value of nThread
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Ottieni il valore di ipClient
     *
     * @return the value of ipClient
     */
    public Inet4Address getIpClient() {
        return ipClient;
    }

    /**
     * Ottieni il valore di dimData
     *
     * @return the value of dimData
     */
    public int getDimensionData() {
        return dimensionData;
    }

    /**
     * Ottieni il valore di clientPorts
     *
     * @return the value of clientPorts
     */
    public int[] getClientPorts() {
        return clientPorts;
    }

    /**
     * Imposta il valore di port
     *
     * @param port new value of port
     */
    public void setPort(int port) {
        if (port >= 1024 && port <= 65535) {
            this.port = port;
        }
    }

    /**
     * Imposta il valore di nThread.
     *
     * @param threads new value of nThread
     */
    public void setThreads(int threads) {
        if (threads > 0) {
            this.threads = threads;
        }
    }

    /**
     * Imposta il valore di ipClient
     *
     * @param ipClient new value of ipClient
     */
    public void setIpClient(Inet4Address ipClient) {
        this.ipClient = ipClient;
    }

    /**
     * Imposta il valore di dimData
     *
     * @param dimensionData new value of dimData
     */
    public void setDimensionData(int dimensionData) {
        if (dimensionData >= 32) {
            this.dimensionData = dimensionData;
        }
    }

    /**
     * Imposta il valore di clientPorts
     *
     * @param clientPorts
     */
    public void setClientPort(int[] clientPorts) {
        this.clientPorts = clientPorts;
    }

    /**
     * Costruttore personalizzato.
     *
     * @param port porta d'ascolto del server
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Metodo che si occupa di attendere un client, inoltre invia e riceve i
     * dati utili per il test.
     *
     * @return se un client si è connesso
     */
    public boolean testClient() {
        try {
            //apertura socket
            ServerSocket svr = setInterface();
            //stampo ip server + porta
            System.out.println("ip: " + svr.getInetAddress());
            System.out.println("porta: " + svr.getLocalPort());
            //ascolto
            Socket serverTCP = svr.accept();
            //copio l'ip del client
            Inet4Address ip = (Inet4Address) serverTCP.getInetAddress();
            setIpClient(ip);
            //preparo componenti per leggere i dati base del client
            InputStream input = serverTCP.getInputStream();
            BufferedReader dataClient = new BufferedReader(
                    new InputStreamReader(input)
            );
            //trasformazione dati base
            String testo = dataClient.readLine();
            String thread = testo.substring(0, testo.indexOf(";"));
            String dimData = testo.substring(testo.indexOf(";") + 1);
            setThreads(Integer.parseInt(thread));
            setDimensionData(Integer.parseInt(dimData));
            //ricevere porte client disponibili
            input = serverTCP.getInputStream();
            dataClient = new BufferedReader(
                    new InputStreamReader(input)
            );
            //trasformazione dati x porte client + creazione di quelle x server
            testo = dataClient.readLine();
            clientPorts = new int[getThreads()];
            serverPorts = new int[getThreads()];
            String[] cli = testo.split(",");
            String svrPorts = ValidateInputs.getAviablePort(port, getThreads() + 1).toString();
            String[] serv = svrPorts.split(",");
            for (int i = 0; i < threads; i++) {
                if (!cli[i].isEmpty() || !serv[i].isEmpty()) {
                    clientPorts[i] = Integer.parseInt(cli[i]);
                    serverPorts[i] = Integer.parseInt(serv[i]);
                }
            }
            //invio porte server
            OutputStream output = serverTCP.getOutputStream();
            PrintWriter data = new PrintWriter(output, true);
            data.println(svrPorts);
            //chiusura socket
            svr.close();
        } catch (IOException | IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Metodo che seleziona la scheda di rete corretta
     * @return il socket impostato correttamente
     * @throws java.io.IOException
     */
    public ServerSocket setInterface() throws IOException {
        try {
            Enumeration<NetworkInterface> interfaccieRete = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface f : Collections.list(interfaccieRete)) {
                if (f.isUp()) {
                    if (!f.getDisplayName().contains("VMware")
                         && !f.getDisplayName().contains("VirtualBox")
                         && !f.getName().contains("lo")) {
                            //System.out.println(f);
                            ServerSocket s = new ServerSocket();
                            Enumeration<InetAddress> fAddress = f.getInetAddresses();
                            s.bind(new InetSocketAddress(fAddress.nextElement(), port));
                            return s;
                    }
                }
            }
        } catch (SocketException se ) {

        }
        return null;
    }

    /**
     * Metodo che crea i processi per poter accettare più connessioni su porte
     * diverse.
     */
    public void createThred() {
        for (int i = 0; i < getThreads(); i++) {
            new ServerThread(
                    serverPorts[i],
                    getIpClient(),
                    clientPorts[i],
                    getDimensionData()
            ).start();
        }
    }

    /**
     * Metodo che si occupa di stampare lo stato del client.
     *
     * @param connacted se il client ha fatto una connessione al server
     * @param trasmetting se il client sta trasmettendo i dati
     * @param seeParam se bisogna mostrare i parametri di connessione
     */
    public void getClientStatus(boolean connacted, boolean trasmetting, boolean seeParam) {
        String ris = "\n";
        if(connacted){
            ris += "Client connesso: " + getIpClient().toString();   
        }else if(trasmetting){
            ris += "Inizio test ...";
        }
        if(seeParam){
            ris += "\nParametri client\n";
            ris += "Numero di connessioni: " + getThreads() + "\n";
            ris += "Dimensini di ogni pacchetto: " + getDimensionData() + "\n";
        }
        System.out.println(ris);
    }
}
