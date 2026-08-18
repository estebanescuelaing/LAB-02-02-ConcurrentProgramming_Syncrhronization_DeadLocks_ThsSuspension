package edu.eci.arsw.blacklistvalidator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class BlackListSearchThread extends Thread {

    private final HostBlacklistsDataSourceFacade skds;
    private final String ipaddress;

    private final int desde;
    private final int hasta;

    private final LinkedList<Integer> ocurrencias = new LinkedList<>();

    private final AtomicInteger ocurrenciasTotales;

    private static final int BLACK_LIST_ALARM_COUNT = 5;

    private final AtomicInteger listasRevisadas;

    public BlackListSearchThread(
            HostBlacklistsDataSourceFacade skds,
            String ipaddress,
            int desde,
            int hasta,
            AtomicInteger ocurrenciasTotales,
            AtomicInteger listasRevisadas) {

        this.skds = skds;
        this.ipaddress = ipaddress;
        this.desde = desde;
        this.hasta = hasta;
        this.ocurrenciasTotales = ocurrenciasTotales;
        this.listasRevisadas = listasRevisadas;
    }

    @Override
    public void run() {

        for (int i = desde; i < hasta; i++) {

            /*
             * Si entre todos los hilos ya encontramos
             * 5 ocurrencias, no tiene sentido continuar.
             */
            if (ocurrenciasTotales.get() >= BLACK_LIST_ALARM_COUNT) {
                return;
            }

            listasRevisadas.incrementAndGet();

            if (skds.isInBlackListServer(i, ipaddress)) {

                ocurrencias.add(i);

                int total = ocurrenciasTotales.incrementAndGet();

                /*
                 * Acabamos de alcanzar el límite.
                 * Este hilo termina su búsqueda.
                 */
                if (total >= BLACK_LIST_ALARM_COUNT) {
                    return;
                }
            }
        }
    }

    public int getOcurrenciasCount() {
        return ocurrencias.size();
    }

    public List<Integer> getBlackListOcurrences() {
        return ocurrencias;
    }
    
   
    

}