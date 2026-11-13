package br.unicesumar.maridodealuguel.service;

/**
 * Distância em linha reta entre dois pontos na superfície da Terra (fórmula de Haversine).
 * A busca faz o mesmo cálculo em SQL; aqui ele é usado para validar a abertura de ordens.
 */
public final class Haversine {

    private static final double RAIO_TERRA_KM = 6371.0;

    private Haversine() {
    }

    public static double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * RAIO_TERRA_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }
}
