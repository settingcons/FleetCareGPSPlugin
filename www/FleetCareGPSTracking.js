var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec'),
    Ruta = require('./Ruta'),
    RutaPunto = require('./RutaPunto'),
    GPSError = require('./GPSError');
/**
 * [FleetCareGPSTracking Este objeto contiene las funciones necesarias para la conexión entre código nativo y JS del plugin]
 * @type {Object}
 */
var FleetCareGPSTracking = {
    rutaActual: null,
    puntosEncontrados: null,
    eventPuntos: null,
    usuarioId: 0,
    intervaloCaptura: 5000,
    mensajeDireccion: '',
    puntosValidos: true,
    callbackPuntos: null,
    callbackRutaGuardada: null,
    callbackRutaFinalizada: null,
    callbackNoCoordenadas: null,
    /**
     * [init description]
     * @param  {int} usuario_id             [description]
     * @param  {int} intervalo              [description]
     * @param  {String} mensaje_direccion   [description]
     * @param  {String} puntos_validos   [description]
     * @param  {Function} callbackRutaGuardada   [description]
     * @param  {Function} callbackPuntos         [description]
     * @param  {Function} callbackRutaFinalizada [description]
     * @param  {Function} callbackNoCoordenadas  [description]
     * @return {void}
     */
    init: function (usuario_id, intervalo, mensaje_direccion, puntos_validos, callbackRutaGuardada, callbackPuntos, callbackRutaFinalizada, callbackNoCoordenadas) {
        /**
         * Seteamos variables necesarias para el plugin
         */
        FleetCareGPSTracking.usuarioId = usuario_id || FleetCareGPSTracking.usuarioId;
        FleetCareGPSTracking.intervaloCaptura = intervalo || FleetCareGPSTracking.intervaloCaptura;
        FleetCareGPSTracking.mensajeDireccion = mensaje_direccion || FleetCareGPSTracking.mensajeDireccion;
        FleetCareGPSTracking.puntosValidos = puntos_validos;

        /**
         * Seteamos las funciones/callbacks que usará el plugin según las diferentes acciones
         */
        FleetCareGPSTracking.callbackRutaGuardada = callbackRutaGuardada;
        FleetCareGPSTracking.callbackPuntos = callbackPuntos;
        FleetCareGPSTracking.callbackRutaFinalizada = callbackRutaFinalizada;
        FleetCareGPSTracking.callbackNoCoordenadas = callbackNoCoordenadas;
        FleetCareGPSTracking.estaActivo(function (status) {
            if (status) {
                FleetCareGPSTracking.getNuevosPuntos(function (p) {
                    FleetCareGPSTracking.callbackPuntos(p);
                }, function (e) {
                    //show error
                });
                FleetCareGPSTracking.getRutalActual(function (ruta) {
                    FleetCareGPSTracking.callbackRutaGuardada(ruta);
                }, function (e) {
                    //show error
                });
            }
        }, function (e) {
        });
    },
    iniciarServicio: function (successCallback, errorCallback) {
        var win = function (msg) {
            successCallback(msg);
        };
        var fail = function (e) {
            var err = new GPSError(e.code, e.message);
            errorCallback(err);
        };
        exec(win, fail, "FleetCareGPSTracking", "iniciarServicio", [{
            'IdUsuario': FleetCareGPSTracking.usuarioId,
            'IntervaloCaptura': FleetCareGPSTracking.intervaloCaptura * 1000,
            'MensajeDireccionNoEncontrada': FleetCareGPSTracking.mensajeDireccion,
            'PuntosValidos': FleetCareGPSTracking.puntosValidos
        }]);
    },
    guardarNuevosPuntos: function (puntos) {
        FleetCareGPSTracking.puntosEncontrados = puntos;
        if (FleetCareGPSTracking.callbackPuntos != null)
            FleetCareGPSTracking.callbackPuntos(FleetCareGPSTracking.puntosEncontrados);
    },
    getNuevosPuntos: function (successCallback, errorCallback) {
        var win = function (p) {
            FleetCareGPSTracking.puntosEncontrados = p;
            successCallback(p);
        };
        var fail = function (e) {
            var err = new GPSError(e.code, e.message);
            errorCallback(err);
        };
        exec(win, fail, "FleetCareGPSTracking", "getNuevosPuntos", []);
    },
    getRutalActual: function (successCallback, errorCallback) {
        var win = function (r) {
            var ruta = r;
            FleetCareGPSTracking.rutaActual = new Ruta(ruta.IdRuta, ruta.IdUsuario, ruta.FechaHoraInicio, ruta.FechaHoraFin, ruta.Duracion, ruta.Distancia, ruta.Observaciones, ruta.DireccionInicio, ruta.DireccionFin, ruta.EsIOS);
            successCallback(FleetCareGPSTracking.rutaActual);
        };
        var fail = function (e) {
            var err = new GPSError(e.code, e.message);
            errorCallback(err);
        };
        exec(win, fail, "FleetCareGPSTracking", "getRutaActual", []);
    },
    estaActivo: function (successCallback, errorCallback) {
        var win = function (activo) {
            successCallback(activo);
        };
        var fail = function (e) {
            var err = new GPSError(e.code, e.message);
            errorCallback(err);
        };
        exec(win, fail, "FleetCareGPSTracking", "activo", []);
    },
    guardarRutaActual: function (ruta, final) {
        ruta = JSON.parse(ruta);
        if (final) {
            FleetCareGPSTracking.rutaActual = new Ruta(ruta.IdRuta, ruta.IdUsuario, ruta.FechaHoraInicio, ruta.FechaHoraFin, ruta.Duracion, ruta.Distancia, ruta.Observaciones, ruta.DireccionInicio, ruta.DireccionFin, ruta.ListaPuntos);
            FleetCareGPSTracking.callbackRutaFinalizada(FleetCareGPSTracking.rutaActual);
        } else {
            FleetCareGPSTracking.rutaActual = new Ruta(ruta.IdRuta, ruta.IdUsuario, ruta.FechaHoraInicio, ruta.FechaHoraFin, ruta.Duracion, ruta.Distancia, ruta.Observaciones, ruta.DireccionInicio, ruta.DireccionFin);
            FleetCareGPSTracking.callbackRutaGuardada(FleetCareGPSTracking.rutaActual);
        }
    },
    avisoNoCoordenadas: function () {
        if (FleetCareGPSTracking.callbackNoCoordenadas != null)
            FleetCareGPSTracking.callbackNoCoordenadas();
    },
    finalizarServicio: function (successCallback, errorCallback) {
        var win = function (msg) {
            successCallback(msg);
        };
        var fail = function (e) {
            var err = new GPSError(e.code, e.message);
            errorCallback(err);
            FleetCareGPSTracking.callbackRutaFinalizada(FleetCareGPSTracking.rutaActual);
        };
        exec(win, fail, "FleetCareGPSTracking", "finalizarServicio", []);
    },
	numeroProcesadores: function(successCallback, errorCallback) {
        var win = function (msg) {
            successCallback(msg);
        };
        var fail = function (e) {
            errorCallback(e);
        };
        exec(win, fail, "FleetCareGPSTracking", "getNumeroDeProcesadores", []);
    },
    rutaNoEncontrada: function(msg) {
        console.info(msg.message);
    }
};
module.exports = FleetCareGPSTracking;