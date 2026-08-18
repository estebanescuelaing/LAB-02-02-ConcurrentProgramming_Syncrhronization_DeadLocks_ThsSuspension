package edu.eci.arsw.blacklistvalidator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;

    /**
     * Versión original (secuencial) — Parte I / código del profesor.
     */
    public List<Integer> checkHost(String ipaddress) {

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();

        int ocurrencesCount = 0;

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        int checkedListsCount = 0;

        

        for (int i = 0; i < skds.getRegisteredServersCount() && ocurrencesCount < BLACK_LIST_ALARM_COUNT; i++) {
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)) {
                blackListOcurrences.add(i);
                ocurrencesCount++;
            }
        }

        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences;
    }

    /**
     * Versión paralela — Parte II. Recibe n, el número de hilos.
     */
    public List<Integer> checkHost(String ipaddress, int n) {

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        int total = skds.getRegisteredServersCount();

        


        if (n <= 0 || n > total) {
            throw new IllegalArgumentException(
                "El número de hilos debe ser mayor a 0 y no puede superar el número de listas negras (" + total + ")");
        }

        int tam = total / n;
        int sobrante = total % n;

        BlackListSearchThread[] hilos = new BlackListSearchThread[n];

        AtomicInteger ocurrenciasTotales = new AtomicInteger(0);

        AtomicInteger listasRevisadas = new AtomicInteger(0);

        int desde = 0;
        for (int i = 0; i < n; i++) {
            int hasta = desde + tam;
            if (i == n - 1) {
                hasta += sobrante;
            }
            hilos[i] = new BlackListSearchThread(skds, ipaddress, desde, hasta, ocurrenciasTotales, listasRevisadas);
            hilos[i].start();
            desde = hasta;
        }

        for (int i = 0; i < n; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.SEVERE, "Interrupción esperando al hilo", e);
            }
        }

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();

        
        for (int i = 0; i < n; i++) {
            blackListOcurrences.addAll(
                hilos[i].getBlackListOcurrences()
            );
        }

        int checkedListsCount = listasRevisadas.get();

        LOG.log(
            Level.INFO,
            "Checked Black Lists:{0} of {1}",
            new Object[]{
                checkedListsCount,
                total
            }
        );

        int ocurrencesCount = ocurrenciasTotales.get();

        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }


        return blackListOcurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
}