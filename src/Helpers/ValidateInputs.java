package Helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.io.IOException;

/**
 * Classe che contine dei metodi che servono per la validazione degli imput
 * e in determinati casi aiuta altre classi.
 * @author Daniele Cereghetti
 * @version 19.11.2020
 */
public class ValidateInputs {
    
    /**
     * Metodo che valida gli imputi di tipo intero.
     * @param domanda stringa che pone una domanda
     * @param error stringa che dice cosa fare in caso di errore
     * @param min numero minimo possibile da inserire
     * @param max numero massimo possibili da inserire
     * @return il valore inserito
     */
    public static int getInfo(String domanda, String error, int min, int max){
        BufferedReader tastiera = new BufferedReader(
                new InputStreamReader(System.in)
        );
        int ris = 0;
        for(boolean c = false; c == false;){
            System.out.print(domanda);
            try{
              ris = Integer.parseInt(tastiera.readLine());
              if(ris > max+1 || ris < min-1){
                  throw new Exception();
              }
              c = true;
            }catch(Exception e){ 
                System.out.println(error);
            }
        }
        return ris;
    }
    
    /**
     * Metodo che valida la porta.
     * @return il numero della porta
     */
    public static int getPort(){
        BufferedReader tastiera = new BufferedReader(
                new InputStreamReader(System.in)
        );
        int port = 0;
        while(true){
            System.out.print("Porta sulla quale ascoltera' il server[1024-->65535]: ");
            try{
                port = Integer.parseInt(tastiera.readLine());
                if(port <= 65535 && port >= 1024){
                    if(testPort(port)){
                        return port; 
                    }
                }else{
                    System.out.println("Errore, inserisci un numero compreso tra 1024 e 65535");
                    throw new IllegalArgumentException();
                }
            }catch(IOException ioe){ 
                System.out.println("Porta già utilizzata scegline una tra queste:");
                StringBuilder ports = getAviablePort(port, 10);
                System.out.println(ports);
            }catch(IllegalArgumentException iae){
                System.out.println("Errore, inserisci un numero compreso tra 1024 e 65535");
            }
        }
    }
    
    /**
     * Metodo che valida se la porta scelta è libera.
     * @param port porta scheda di rete
     * @return porta non uccupata da altri processi
     * @throws IOException 
     */
    public static boolean testPort(int port) throws IOException{
        ServerSocket s = new ServerSocket(port);
        s.close();
        return true;
    }
    
    /**
     * Metodo che ritorna delle porte disponibili vicine a quella scelta.
     * @param port porta da cui inizaire
     * @param n il numero di porte richieste
     * @return stringa di porte libere
     */
    public static StringBuilder getAviablePort(int port, int n){
        StringBuilder string = new StringBuilder();
        for(int i = 0; i < n; i++){
            try{
                if(testPort(port+i)){
                    string.append(port+i).append(",");
                }
            }catch(IOException io){ }
        }
        return string;
    }
    
    /**
     * Metodo che valida un ip
     * @return un'ip valido
     */
    public static Inet4Address getIpServer(){
        BufferedReader tastiera = new BufferedReader(
                new InputStreamReader(System.in)
        );
        while(true){
           try{
                System.out.print("Inserisci l'ip del server[format: 10.20.4.118]"
                        + "[aiuto: -h]:");
                String ip = tastiera.readLine();
                if(ip.equals("-h")){
                    System.out.println("vai sul terminale del server e scrivi ipconfig se l'OS è windows");
                    System.out.println("altrimenti ifconfig se è un'altro OS.");
                }else{
                    return (Inet4Address) Inet4Address.getByName(ip);
                }
            }catch(IOException io){
                System.out.println("Errore tastiera");
            } 
        }
    }
    
}
