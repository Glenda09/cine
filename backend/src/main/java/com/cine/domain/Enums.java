package com.cine.domain;

public class Enums {
    public enum TipoPelicula { DOSD, TRESD, IMAX }
    public enum ClasificacionEdad { TP, _7PLUS, _12PLUS, _15PLUS, _18PLUS }
    public enum EstadoPelicula { ACTIVA, INACTIVA }
    public enum Idioma { SUB, DOB }
    public enum Formato { DOSD, TRESD, IMAX }
    public enum TipoAsiento { NORMAL, PREFERENCIAL, DISCAPACITADO }
    public enum EstadoAsientoFuncion { LIBRE, HOLD, RESERVADO, COMPRADO }
    public enum EstadoReserva { PENDIENTE, EXPIRADA, PAGADA, CANCELADA }
    public enum RolUsuario { USER, ADMIN }
    public enum ProveedorPago { WOMPI }
    public enum EstadoPago { CREATED, PENDING, APPROVED, DECLINED }
}

